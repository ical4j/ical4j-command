# Design — single-native-binary

## Context

The repo today:

| Module | Content | Tests |
|---|---|---|
| ical4j-command-core | 5 vocabulary enums | none |
| ical4j-command-cli | 82 files, 64 picocli commands, pre-vocabulary names | 13 Spock specs, all pass, 12 commands covered |
| ical4j-command-mcp | Spring Boot + Spring AI, 18 tools returning literal strings | none |
| ical4j-command-agent | 4 files, excluded from `settings.gradle` | 1 |

The user wants one native executable, `ict`, with MCP served by `ict mcp`. Native-image difficulty lives in dependencies, not in this code, so the repo-shape question and the native question are decided separately here.

Constraints:
- ical4j 4.x is non-negotiable and uses `ServiceLoader` for factories plus classpath resources for timezones.
- MCP stdio transport owns stdout; any stray write corrupts the protocol.
- The build and publishing infrastructure (Gradle 9, axion-release, vanniktech, CodeQL, scorecards) was modernised in August 2026 and is worth keeping.

## Goals / Non-Goals

**Goals**
- One Gradle module, one binary, sub-100ms startup for `ict --help`.
- CLI and MCP defined once via an operation registry.
- Every operation in the new tree has a test and passes a native smoke run.
- Keep git history, CI, and publishing infrastructure.

**Non-Goals**
- Preserving current command names or the agent module.
- MCP transports other than stdio (HTTP/SSE can come later as `ict mcp --transport http`).
- A Homebrew tap, SDKMAN candidate or signed installers. GitHub Releases is enough for this change.
- Native metadata contributions upstream to ical4j. Metadata lives in this repo under `META-INF/native-image` until proven stable.

## Decisions

### D1 — Restructure in place, with fresh-start discipline
Work on a branch in this repo. Delete the mcp, agent and core modules rather than migrating them. Existing CLI command classes are moved into the new tree one at a time, each with a test.

- **Why:** a fresh repo only buys permission to delete, which a branch grants too. A fresh repo loses the August build and publishing work, CI, history and the repo identity, and leaves the old repo half-alive.
- **Alternative rejected:** fresh repository. Also rejected: keeping all 64 commands and retrofitting names, which would carry 52 untested commands into the native build.

### D2 — One operation registry, two views (reverses spec Option B)
Each operation is a single `Operation` value: `(Verb, EntityType, Locus)`, a parameter descriptor list, a handler `(Context, Params) -> Result`, and a result type. The CLI view turns each operation into a picocli subcommand and each parameter into an option or positional. The MCP view turns each operation into a tool with a JSON schema generated from the same descriptors.

- **Why:** in one binary two dispatch paths for the same tuple is duplication with drift risk. The spec's Option B was justified by picocli and Spring AI having incompatible dispatch models; Spring AI is gone.
- **Consequence:** the `command-vocabulary` spec's "dispatch independently" requirement is removed and the "shared core module" requirement is rewritten.
- **Alternative rejected:** keep picocli classes as the source of truth and reflect over their annotations to build MCP tools. Reflection is what native image punishes, and picocli's model is CLI-specific.

### D3 — Handlers return structured results; surfaces render
Handlers return domain objects (`Calendar`, `VCard`, lists of `Uid`, collection descriptors, plain status records), never formatted strings. The CLI renders to iCalendar/vCard text or a table; MCP renders to text or JSON content blocks. This generalises the existing `AbstractCommand<T>` output-handler idea.

- **Why:** the same operation must serve a terminal and a model. Formatting inside the handler forces one of them to parse the other's output.

### D4 — Verb-first CLI syntax
`ict <verb> <type> [--workspace W] [--collection C] [args]`. Loci are options, not positionals. `generate <type>` takes no locus. `import|export <family>` and `send|receive` follow the same pattern. Locus management stays as `ict add collection`, `ict list workspace` and so on, since workspace and collection are also types the vocabulary already names.

- **Why:** matches the spec tuple order and the user's choice. Help output groups by verb.
- **Alternative considered:** type-first (`ict event list`), which groups help by type. Purely UX; both derive from the registry.

### D5 — MCP via the MCP Java SDK, programmatic registration, stdio
Use `io.modelcontextprotocol.sdk:mcp` with `McpServer.sync(...)` and `SyncToolSpecification` built from the registry. Tool names are `<verb>_<type>` (for example `list_event`). No annotation-driven tool discovery.

- **Why:** the SDK tracks the protocol; programmatic registration keeps reflection out; Jackson is already on the classpath through ical4j-serializer.
- **Alternatives rejected:** Spring AI (Spring Boot runtime, AOT complexity, 100MB+ binaries); Quarkus mcp-server-stdio (absorbs native metadata pain but brings a full framework into a CLI; the T0 spike showed the metadata burden is 13 entries, so the fallback is not needed); hand-rolled JSON-RPC (protocol drifts).

### D6 — GraalVM native via `org.graalvm.buildtools.native`, decided by the T0 spike
**Decision (task 0.4, 2026-09-05): proceed with plain GraalVM. Quarkus is not needed.** The spike (`docs/native-spike.md`) compiled the current cli module on GraalVM CE 25.3.4 and found the whole dependency set needs 13 hand-written metadata entries: one type for night-config's `TomlFormat`, two constructors for ical4j's timezone registry and cache, one constructor for Jackson, and resource globs for ical4j's zoneinfo trees and alias files. The result is a single 51 MB file with 30 ms startup against 350 ms on the JVM.

Concrete choices carried forward from the spike:
- **GraalVM CE for JDK 25 or later.** The plugin's shared reachability-metadata repository refuses GraalVM for JDK 21. Toolchain detection is off; the build reads `GRAALVM_HOME`.
- **`sharedLibrary = false` set explicitly.** Plugin 1.1.12 passed `--shared` otherwise and produced a dylib.
- **picocli's runtime generators, not the annotation processor.** picocli-codegen's processor fails on `@ArgGroup` fields in abstract superclasses. `ReflectionConfigGenerator`, `ResourceConfigGenerator` and `DynamicProxyConfigGenerator` build the model the way the app does. For the registry-driven tree they remain the right tool because commands are not annotated classes.
- **Never register a JDK type wholesale.** picocli registers every option type with all public methods; a `java.net.URL` option made `URL.getContent()` a reflective root, whose JDK content handlers for images and audio live in `java.desktop`, dragging AWT and `javax.sound` into the image and 11 dylibs next to the binary. The generated config strips that entry. Registry parameter types are string, path, integer, boolean and enum, converted inside handlers, so this cannot recur.
- **Metadata sources.** A small hand-written base file under `src/main/resources/META-INF/native-image/org.ical4j/ict/reachability-metadata.json` holds the 13 entries. The tracing agent is driven from the native smoke suite's command list on the JVM build, not from the Spock run, which drowns the signal in Groovy introspection and test-framework service lookups. Agent output merges on top of the base file and is reviewed before commit.
- **The JVM `installDist` path stays buildable** for debugging, for the agent run, and for platforms without a native runner.

- **Why:** standard, supported path; picocli is native-first; the measured cost is small.
- **Alternative rejected:** Quarkus with its picocli and mcp-server-stdio extensions. It would absorb the metadata work, but that work turned out to be 13 lines, and Quarkus adds a build-time framework and a much larger dependency surface to a CLI.

### D7 — Logging: drop log4j2, everything to stderr
Replace log4j-core and the slf4j bridge with `slf4j-simple` configured to stderr, level controlled by `--verbose`/`--quiet` and `ICT_LOG_LEVEL`. picocli's out/err are set explicitly per subcommand; `ict mcp` binds picocli's out to stderr.

- **Why:** stdout exclusivity for MCP; log4j is heavy for a CLI and its native metadata is one more thing to own.

### D8 — Admission bar
An operation enters `ops/` only with: a Spock spec against an injected in-memory or temp-dir store, and inclusion in the native smoke test that runs `ict <verb> <type> --help` and one real invocation. First cohort: the operations behind the 13 green specs.

- **Why:** this is the mechanism that gives the fresh-start effect without a fresh repo.

### D9 — Distribution
GitHub Releases carry `ict-<version>-<os>-<arch>` binaries from a CI matrix: ubuntu x64, ubuntu arm64, macOS arm64, windows x64. The single jar continues to publish to Maven Central with the existing vanniktech config, so an embedding consumer remains possible without extra work.

- **Why:** the binary is the product; keeping the jar costs nothing now that publishing is configured.

### D10 — Package layout
```
src/main/java/org/ical4j/command/
  vocab/     Verb, EntityType, Locus, SerializationFamily, SendMethod (moved from core)
  ops/       Operation, Param, Result, Registry, handlers by type
  cli/       Main (ict), picocli view builder, renderers
  mcp/       McpCommand, tool builder, content renderers
  config/    CommandConfig (night-config TOML), Filesystem
```

## Risks / Trade-offs

- [ical4j needs extensive resource and service metadata] → measured in T0: two constructor entries and resource globs; ServiceLoader factories resolve automatically. Contribute the entries upstream to ical4j's bundled `META-INF/native-image`.
- [Jackson XML via ical4j-serializer drags woodstox and reflective types into the image] → measured in T0: XML and JSON conversion work with one Jackson entry plus the plugin's metadata repository. Keep the formats.
- [MCP SDK pulls reactor-core, which has its own native quirks] → not exercised by the spike; covered by the tracing agent in task 4/5; SDK is used by Spring AI native builds so metadata exists upstream.
- [Pre-existing JVM bugs hide behind untested commands] → the spike found `ict list` and `print-workspace` failing to initialise collection config, vCard JSON export cycling in Jackson, and the filter grammar rejecting a basic expression. The admission bar (D8) keeps these out until each has a test.
- [Native CI matrix is slow and runner-expensive] → run native builds on tags and a nightly schedule only; PR builds run JVM tests plus a single linux native smoke.
- [Dropping 52 commands loses in-progress ideas] → they stay in git history and are listed in `docs/dropped-commands.md` with their intended vocabulary tuple so they can be re-admitted.
- [Removing JPMS `module-info` breaks nothing today but the bnd/OSGi packaging goes with it] → accepted; OSGi bundles were never a stated goal of the tool.

## Migration Plan

1. Done: T0 spike on the current `ical4j-command-cli` module (branch `spike/native-cli`), findings in `docs/native-spike.md`, decision recorded in D6.
2. Collapse modules and dependencies (T1). Build stays green with zero operations registered.
3. Registry, CLI view, first cohort of operations with tests (T2, T3).
4. `ict mcp` (T4). Verified with a scripted stdio client in tests.
5. Native build and metadata (T5). Smoke test in CI.
6. Release matrix and docs (T6, T7). Tag the first release.

Rollback is a branch delete; `develop` is untouched until merge.

## Open Questions

- Should `ict mcp` expose every registered operation, or an allowlist? Default: every operation with a `mcp: true` flag on the descriptor, so destructive operations can be held back initially.
- Does `generate` belong in the first release, or wait until the strategy classes get tests?
