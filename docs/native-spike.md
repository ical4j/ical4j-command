# Native image spike (single-native-binary, task 0)

Date: 2026-09-05. Branch: `spike/native-cli` (throwaway, off `single-native-binary`).
Goal: compile the current `ical4j-command-cli` module with GraalVM native-image before
restructuring anything, so the cost of the dependency set is known.

## Result

`./gradlew :ical4j-command-cli:nativeCompile` produces a working `ict` executable.

| Measure | Native (GraalVM CE 25.3.4, macOS arm64) | JVM (Temurin 17, installDist) |
|---|---|---|
| `ict --help` wall time | 20–30 ms | 350–370 ms |
| Binary size | 50 MB, plus ~5 MB of AWT dylibs copied alongside | n/a |
| Compile time | 1 m 45 s, peak RSS 2.7 GB | n/a |

Working natively without any extra metadata: `--version`, `--help` on every subcommand,
`validate -file`, `convert --help`, `filter --help`, `generate --help`.

## Toolchain findings

1. **GraalVM version.** `org.graalvm.buildtools.native` 1.1.12 refuses to use its shared
   reachability-metadata repository with GraalVM for JDK 21 (schema too old). Either run
   GraalVM for JDK 23+ or pass `-PnoMetadataRepo` (added to the cli build) to disable the
   repository. GraalVM CE 25.3.4 is the version to standardise on.
2. **Shared-library default.** With plugin 1.1.12 and this build script the native-image
   command line carried `--shared` even though `mainClass` was set, producing `ict.dylib`.
   Setting `sharedLibrary = false` explicitly fixes it. Keep that line.
3. **picocli-codegen annotation processor fails on this tree.** Error:
   `ArgGroup has no options or positional parameters ... FIELD input in AbstractEntityCommand`.
   The `@ArgGroup InputOptions` fields live in abstract, non-`@Command` superclasses, which
   the processor does not model. picocli's runtime generators
   (`ReflectionConfigGenerator`, `ResourceConfigGenerator`, `DynamicProxyConfigGenerator`)
   build the model the way the app does and work: 102 reflect entries generated. The cli
   build now runs them as `generatePicocliMetadata` and feeds the output dir to the image
   classpath. For the new registry-driven tree (design D2) the generators stay the right
   tool because commands will not be annotated classes at all.
4. **Deprecated flags.** GraalVM 25 warns that `--no-fallback` has no effect and that
   `proxy-config.json` is deprecated in favour of `reachability-metadata.json`. Harmless.

## Dependency findings

| Dependency | Native status | Action |
|---|---|---|
| picocli 4.7.7 | works with generated metadata | keep; runtime generators in build |
| ical4j 4.2.3 | parses and validates; ships its own `META-INF/native-image` (tz.alias resource + 2 reflect entries). Still logs `Error loading timezone aliases: inStream parameter is null` twice at startup, and the 618-file `zoneinfo` / `zoneinfo-global` trees are not registered, so VTIMEZONE generation will fail natively until they are | add resource-config for `zoneinfo/**`, `zoneinfo-global/**`, and check why `tz.alias` is not found despite the bundled pattern; ServiceLoader factories (`PropertyFactory` etc.) resolved without extra work |
| night-config 3.8.3 (toml) | **breaks** any command touching `CommandConfig`: `NoFormatFoundException: No suitable format for ictrc.toml`. `FormatDetector` locates `TomlFormat` via `Class.forName`, which is invisible to the image | one-line code fix: build `FileConfig` with `TomlFormat.instance()` explicitly, or a reflect entry for `com.electronwill.nightconfig.toml.TomlFormat` |
| log4j2 2.25.2 | initialises and logs to stderr natively (metadata repository supplies config) | still replace per design D7; it is 2 of the 35 jars and owns stdout/stderr routing |
| jackson databind + dataformat-xml + woodstox | image builds; not exercised at runtime (`convert` needs a valid input file) | exercise in task 0.2 with the tracing agent; repository has metadata for databind 2.15 and dataformat-xml 2.22 |
| commons-lang3 3.18.0 | only jar on the classpath referencing `java.desktop` (via `java.beans`); first suspected as the cause of the AWT dylibs | hypothesis disproved in task 0.3, see below: the cause is picocli's reflect-config for `java.net.URL` |
| `module-info.java` `requires java.desktop` | not the cause of the AWT libs; nothing in this repo uses AWT | remove module-info with the restructure |

## Pre-existing (not native) issues surfaced

- `src/test/resources/geo.ics` fails to parse on the JVM too (`Invalid GEO string`), so it is
  not a valid smoke input. Need a known-good fixture.
- `ict generate event` declares `--summary` and `--description` as flags without values and
  rejects any actual arguments. Confirms the strategy commands belong to the untested set.
- `ict strategy` prints nothing and exits 0.

## Task 0.2: tracing agent

Two captures were taken on GraalVM CE 25 (JDK 25 ran the Groovy 3 / Spock suite without
trouble):

| Capture | How | Reflection entries | Resources | Signal |
|---|---|---|---|---|
| Test suite | `./gradlew -Pagent :ical4j-command-cli:test` with a caller filter excluding Spock, Groovy, JUnit and Gradle (`src/test/resources/native-agent/caller-filter.json`) | 507 | 52 | noisy: 243 log4j entries, Groovy `*BeanInfo`/`*Customizer` introspection, test-framework service files. The caller filter trims reflection but resource lookups still leak because the JDK is the caller |
| Manual invocations | JVM `installDist` launched with `-agentlib:native-image-agent=config-merge-dir=...` running `validate`, `convert` (JCAL, XCAL, JSCALENDAR), `list-workspaces`, `--version`, a vCard `validate`, and a calendar whose `TZID`s have no `VTIMEZONE` | 369 | 44 | clean: `TomlFormat`, `tz.alias`, `msTimezoneIds/Names`, `zoneinfo/tz.availableIds`, the two `zoneinfo/*.ics` files touched, StAX factories for XML |

Merged into `src/main/resources/META-INF/native-image/org.ical4j/ict/reachability-metadata.json`
(447 reflection entries, 24 resource globs) with test-framework noise dropped and the two
per-file zoneinfo entries replaced by `zoneinfo/**` and `zoneinfo-global/**`, since any TZID can
be requested at runtime.

Rebuilt with that file, the native binary passes everything that failed in task 0.1:

| Command | Before | After |
|---|---|---|
| `ict list-workspaces`, `ict list --help` | `NoFormatFoundException` from night-config | works (reflect entry for `TomlFormat`) |
| `ict validate` on a calendar with TZIDs but no VTIMEZONE | untested | works, no alias warning (zoneinfo globs) |
| `ict convert --format XCAL` / `JCAL` / `JSCALENDAR` | untested | works (StAX service files, Jackson entries) |
| `ict validate --mode VCARD` | untested | works |
| `ict --help` | 20–30 ms | 30 ms, 54 MB binary |

Lessons for the real build (design D6, task 5.2):
- Do not derive metadata from the Spock run. Drive the tracing agent from the native smoke
  suite's command list on the JVM build instead, so the capture is exactly the surface the
  binary must support and carries no test-framework noise.
- Keep a small hand-written base file (`zoneinfo/**`, `zoneinfo-global/**`, `tz.alias`,
  `TomlFormat` until the code fix lands) and let the agent output merge on top.
- The AWT dylibs are unchanged (11 files next to the binary); that is a reachability question
  for `java.beans` via commons-lang3, still open and still not blocking.

Pre-existing JVM failures found while capturing, none native-related:
- `ict list` and `ict print-workspace` throw `Unable to initialise collection config` on the JVM.
- `ict convert --format JCARD` on a vCard fails in Jackson with a self-reference cycle through
  `VCard.entities`, so the vCard JSON path in ical4j-serializer is broken independent of native.
- `ict filter --filter "summary contains 'Spike'"` rejects that expression; the grammar needs
  checking before the `filter` verb is admitted.

## Task 0.3: per-dependency cost, measured

To separate what each dependency really needs from what the agent happened to record, the
447-entry merged file was replaced by a hand-written one and the binary rebuilt. Everything
that failed in 0.1 passes with this, 13 entries in total:

```json
{
  "reflection": [
    { "type": "com.electronwill.nightconfig.toml.TomlFormat" },
    { "type": "net.fortuna.ical4j.model.TimeZoneRegistryImpl", "methods": [{ "name": "<init>", "parameterTypes": [] }] },
    { "type": "net.fortuna.ical4j.util.MapTimeZoneCache",       "methods": [{ "name": "<init>", "parameterTypes": [] }] },
    { "type": "com.fasterxml.jackson.databind.ext.Java7SupportImpl", "methods": [{ "name": "<init>", "parameterTypes": [] }] }
  ],
  "resources": [
    { "glob": "ictrc.default.toml" }, { "glob": "simplelogger.properties" }, { "glob": "ical4j.properties" },
    { "glob": "net/fortuna/ical4j/model/tz.alias" }, { "glob": "tz.alias" },
    { "glob": "zoneinfo/**" }, { "glob": "zoneinfo-global/**" },
    { "glob": "net/fortuna/ical4j/transform/compliance/msTimezoneIds" },
    { "glob": "net/fortuna/ical4j/transform/compliance/msTimezoneNames" }
  ]
}
```

| Dependency | What it actually needs natively | Failure without it | Better fix in the new tree |
|---|---|---|---|
| night-config toml | one type entry for `TomlFormat` (`FormatDetector` probes it with `Class.forName`) | `NoFormatFoundException` on any config access | pass `TomlFormat.instance()` to `FileConfig.builder`, then no entry at all |
| ical4j | constructor entries for `TimeZoneRegistryImpl` and `MapTimeZoneCache` (both instantiated by name from `ical4j.properties` defaults), resource globs for `zoneinfo/**`, `zoneinfo-global/**`, `tz.alias`, `msTimezone*`, `ical4j.properties` | `ClassNotFoundException: TimeZoneRegistryImpl` when a TZID has no in-file VTIMEZONE; alias warnings at startup | contribute the entries upstream to ical4j's bundled `META-INF/native-image` (it already ships `tz.alias`), or keep them in our base file |
| jackson databind | one constructor entry for `Java7SupportImpl`; everything else comes from the plugin's metadata repository | none observed, entry captured by the agent | keep; it is one line |
| jackson-dataformat-xml + woodstox | nothing beyond the metadata repository; StAX providers resolve through automatic ServiceLoader support | none | keep as is while `--format XCAL` exists |
| log4j2 | nothing beyond the metadata repository plus our `log4j2.properties` resource | none | still replace (design D7), for stdout discipline not for native reasons |
| picocli | generated reflect-config, minus one entry (see below) | commands not instantiable | keep the runtime generators |

### The AWT libraries were picocli's doing

The 11 dylibs (`libawt`, `libfontmanager`, `libfreetype`, `libjsound`, ...) copied next to the
binary were not caused by commons-lang3. `-H:+PrintAnalysisCallTree` shows two roots:

- `java.net.URL.getContent()` marked as a reflective root method. picocli's generated
  reflect-config registers every option type with `allPublicMethods`, and `InputOptions` has a
  `-url` option of type `java.net.URL`. The JDK's `ContentHandler`s for images and audio live in
  `java.desktop` (`sun.awt.www.content.*`), so `getContent` reaches `Toolkit`,
  `GraphicsEnvironment` and `javax.sound.sampled.AudioSystem`.
- Once those classes are in the analysis universe, type-hierarchy analysis adds
  `sun.awt.SoftCache` behind `Map.clear()` and `EventDispatchThread` behind `Runnable.run()`.

Removing the `java.net.URL` entry from the generated reflect-config (now done in a `doLast` on
`generatePicocliReflectConfig`) drops every dylib. The binary is a single 51 MB file, `-url`
still works because picocli converts the string with `new URL(String)` directly, and startup is
unchanged at 30 ms. For the registry-driven tree the lesson is: parameter types are
`String`/`Path`/enum, converted in the handler, so no JDK type is ever registered wholesale.

## Decision for task 0.4

Proceed with plain GraalVM (design D6). The dependency set is not hostile: the only hard
failure is a `Class.forName` in night-config with a one-line fix, and the remaining work is
resource registration for ical4j's zoneinfo, which the tracing agent will capture. Quarkus is
not needed.
