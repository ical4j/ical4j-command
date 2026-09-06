## 0. Native spike (before restructuring)

- [x] 0.1 On a throwaway branch add `org.graalvm.buildtools.native` and picocli-codegen to the current `ical4j-command-cli` module
- [x] 0.2 Run the tracing agent through the 13 Spock specs and a handful of manual `ict` invocations; collect metadata
- [x] 0.3 Attempt `nativeCompile`; record every failing dependency (ical4j, jackson, woodstox, log4j, night-config) and the fix or workaround in `docs/native-spike.md`
- [x] 0.4 Decide from the findings: proceed with plain GraalVM (D6) or switch to Quarkus (D5 fallback); record the decision in `design.md`

## 1. Collapse the repo

- [x] 1.1 Branch `single-native-binary` from `develop`
- [x] 1.2 Delete `ical4j-command-mcp`, `ical4j-command-agent`, `model-context-protocol`; remove Spring, Spring AI, langchain4j from `libs.versions.toml`
- [x] 1.3 Move `ical4j-command-core` enums into `src/main/java/org/ical4j/command/vocab`; delete the core module
- [x] 1.4 Move `ical4j-command-cli` sources to the root module; `settings.gradle` includes nothing; single `build.gradle` with `application`, native plugin (`sharedLibrary = false`, `toolchainDetection = false`), picocli runtime generators from the spike branch, vanniktech coordinates `org.ical4j:ict`
- [x] 1.5 Remove `module-info.java` files and the bnd/OSGi plugin and annotation processors
- [x] 1.6 Replace log4j-core and log4j-slf4j2 with slf4j-simple to stderr; delete `log4j2.properties`
- [x] 1.7 Quarantine every existing command class under `src/legacy/` (excluded from compilation) so they can be re-admitted one by one; build is green with an empty registry
- [x] 1.8 Write `docs/dropped-commands.md` listing each quarantined command with its intended (verb, type, locus)

## 2. Operation registry

- [x] 2.1 Define `Operation`, `Param`, `ParamType`, `Result` and `Context` in `ops/`; settle the vocabulary gaps listed at the end of `docs/dropped-commands.md` (no-locus form, `channel` locus, workspace/collection as listable types)
- [x] 2.2 Implement `Registry` with duplicate-tuple rejection and a per-surface exposure flag
- [x] 2.3 Implement `Context` construction from `CommandConfig`: active workspace, calendar store, card store
- [x] 2.4 Registry conformance test: every registered tuple has a Spock spec referencing it
- [ ] 2.5 Port first cohort handlers with their existing specs: add/get/remove collection, list collections, import/export/remove calendar, import/export/remove card, list in collection
- [ ] 2.6 Port `DefaultOutputHandlers` logic into surface-neutral renderers (`text`, `json`, `ical`, `vcard`)

## 3. CLI surface

- [ ] 3.1 Implement the picocli tree builder: one subcommand per verb, one per (verb, type) under it, options from `Param` descriptors
- [ ] 3.2 Global options: `--workspace`, `--collection`, `--format`, `--verbose`, `--quiet`, `--version`; picocli out/err wired explicitly
- [ ] 3.3 Exit-code mapping: 0 success, 1 operation failure, 2 usage error
- [ ] 3.4 `ict config` command ported from `ConfigureCommand`
- [ ] 3.5 Spock specs for the tree builder: help grouping, option derivation, exit codes, stdout/stderr separation
- [ ] 3.6 End-to-end spec running `Main` in-process against a temp-dir store for each first-cohort operation

## 4. MCP surface

- [ ] 4.1 Add `io.modelcontextprotocol.sdk:mcp`; implement `ict mcp` with `McpServer.sync` on stdio
- [ ] 4.2 Tool builder: `<verb>_<type>` name, JSON schema from `Param` descriptors, exposure flag respected
- [ ] 4.3 Content renderers: calendar/card as text, lists and descriptors as JSON text, handler errors as `isError` results
- [ ] 4.4 Route picocli output and logging to stderr while `mcp` runs
- [ ] 4.5 Spec with a scripted stdio client: initialize, tools/list, tools/call for a first-cohort tool, clean shutdown on EOF
- [ ] 4.6 Spec asserting every stdout line is valid JSON-RPC while verbose logging is on

## 5. Native build

- [ ] 5.1 Configure `org.graalvm.buildtools.native` and the picocli runtime generators (with the `java.net.URL` strip) in the single module; `nativeCompile` produces a single-file `ict` with no dylibs
- [ ] 5.2 Seed `META-INF/native-image/org.ical4j/ict/reachability-metadata.json` with the 13-entry base file from the spike (`simplelogger.properties` replaces the `log4j2.properties` glob); drive the tracing agent from the smoke suite's command list on the JVM build, review, and merge on top
- [ ] 5.3 Native smoke suite: `--help` plus one invocation per registered operation and one `ict mcp` handshake; fails on an operation missing from the suite
- [ ] 5.4 Verify timezone resources and ServiceLoader factories in the native binary (export with `Australia/Melbourne`, parse `X-` property)
- [ ] 5.5 Startup measurement script reporting median `ict --help` time, failing over 100 ms

## 6. CI and distribution

- [ ] 6.1 `build.yml`: JVM tests on PRs plus a single linux x64 native smoke job
- [ ] 6.2 `release.yml`: on `ict-*` tags, native matrix (linux x64, linux arm64, macOS arm64, windows x64), checksums, GitHub Release upload
- [ ] 6.3 `publish-snapshots.yml`: publish the single `org.ical4j:ict` jar; drop the multi-module assumptions
- [ ] 6.4 Update `Makefile` targets (`native`, `smoke`, `release`) and remove the S3 `uploadDist` target
- [ ] 6.5 Update axion-release tag prefix to `ict-`

## 7. Docs

- [ ] 7.1 Rewrite `README.md` around install, verb-first usage, `--format`, and `ict mcp` client configuration
- [ ] 7.2 Rewrite `docs/command-protocols.md` to describe the registry and the two views; remove per-class listings
- [ ] 7.3 Archive: update `openspec/specs/command-vocabulary/spec.md` via `openspec archive` once tasks are done
