## Why

`ict` is meant to be a tool people run, not a framework people embed, yet the repo is shaped as four Maven artifacts (core, cli, mcp, agent) with a Spring Boot MCP server that returns placeholder strings and an agent module excluded from the build. A single GraalVM native binary with `mcp` as a subcommand gives instant startup, one install, and one place where CLI and MCP tools are defined. The June 2026 vocabulary change deliberately kept CLI and MCP dispatch separate because they were separate deployables; in one binary that separation is pure duplication and needs to be reversed.

## What Changes

- **BREAKING** Collapse the four Gradle modules into a single module producing one executable, `ict`, built as a GraalVM native image. The `ical4j-command-core`, `ical4j-command-cli`, `ical4j-command-mcp` and `ical4j-command-agent` artifacts stop existing.
- **BREAKING** Reverse the vocabulary spec's "shared names, separate dispatch" rule (Option B). CLI subcommands and MCP tools become two views over one **operation registry** keyed by (verb, type, locus). Each operation is defined once with its parameters and handler.
- **BREAKING** Redesign the CLI syntax as verb-first: `ict <verb> <type> [locus options]`, for example `ict list event --collection work`. Pre-vocabulary names (`mkcol`, `rmcol`, `import-calendar`, `validator`, `serialize`, `freebusy`, ...) are removed.
- Add `ict mcp` as a subcommand that serves the same registry over MCP stdio transport, using the MCP Java SDK with programmatic tool registration. Spring Boot and Spring AI are removed.
- Replace log4j2 with a minimal logger writing to stderr, so no code path can write to stdout while MCP owns it.
- Adopt an admission bar for migrating existing commands: an operation enters the new registry only with a passing test and a passing native-image smoke run. The 13 currently green Spock specs (collection, calendar and card CRUD, list, output handlers) define the first cohort. Untested channel, revision, metadata and strategy commands are dropped from the tree and remain in git history.
- Distribute native binaries per platform via GitHub Releases. Maven Central publishing continues for the single jar using the existing vanniktech configuration, but the binary is the primary deliverable.

## Capabilities

### New Capabilities
- `operation-registry`: one definition per (verb, type, locus) operation, with parameters, handler and structured result, from which every surface derives its interface.
- `cli-surface`: the verb-first `ict` command tree, locus options, rendering of results to the terminal, exit codes.
- `mcp-surface`: the `ict mcp` subcommand, tool derivation from the registry, stdio transport discipline.
- `native-binary`: the GraalVM native build, reachability metadata handling, JVM fallback, release packaging.

### Modified Capabilities
- `command-vocabulary`: remove the "dispatch independently" requirement and replace the "shared core module" requirement with the vocabulary living in the single module's operations package. Verbs, types, loci and families are unchanged.

## Impact

- **Repo layout**: `settings.gradle` drops all includes; one `build.gradle` with `application`, `org.graalvm.buildtools.native` and picocli-codegen. `ical4j-command-mcp`, `ical4j-command-agent` and `model-context-protocol` directories are deleted. `module-info.java` files are removed (native-image ignores JPMS and `requires java.desktop` is unexplained).
- **Dependencies removed**: spring-boot, spring-ai, langchain4j, log4j-core, log4j-slf4j2-impl, bnd/OSGi annotation processors. **Added**: `io.modelcontextprotocol.sdk:mcp`, `info.picocli:picocli-codegen`, `org.graalvm.buildtools.native`, an slf4j binding that writes to stderr.
- **Dependencies kept but needing native metadata**: ical4j (ServiceLoader factories, zoneinfo resources), jackson via ical4j-serializer and the MCP SDK, night-config, reactor-core via the MCP SDK.
- **CI**: build workflow gains a native build matrix (linux x64 and arm64, macOS arm64, windows x64). Snapshot publishing shrinks to one artifact. Release workflow uploads binaries to GitHub Releases.
- **Docs**: README and `docs/command-protocols.md` rewritten around the verb-first syntax and `ict mcp`.
- **Users**: anyone scripting the current `ict` command names must migrate. Given the tool has no tagged release, this is accepted without a compatibility shim.
