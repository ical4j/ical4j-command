# ict

`ict` is a command-line tool and MCP server for iCalendar and vCard data, built on
[iCal4j](https://github.com/ical4j/ical4j). It ships as a single native executable: one file, no
JVM required, and about 10 ms to start.

The same operations are available two ways:

- **From a terminal**: `ict <verb> <type> [options]`, for example `ict list event --collection work`.
- **To an AI assistant**: `ict mcp` serves each operation as a Model Context Protocol tool over stdio.

Both are generated from one operation registry, so every operation, its parameters and its
behaviour are identical in each.

## Install

Download the executable for your platform from
[GitHub Releases](https://github.com/ical4j/ical4j-command/releases). Each release carries:

| File | Platform |
|---|---|
| `ict-<version>-linux-x64` | Linux, x86-64 |
| `ict-<version>-linux-arm64` | Linux, ARM64 |
| `ict-<version>-macos-arm64` | macOS, Apple silicon |
| `ict-<version>-windows-x64.exe` | Windows, x86-64 |
| `ict-<version>-checksums.sha256` | SHA-256 checksums for the above |

```sh
sha256sum --check --ignore-missing ict-1.0.0-checksums.sha256
install -m 755 ict-1.0.0-linux-x64 ~/.local/bin/ict
ict --version
```

On macOS, a downloaded binary is quarantined until you allow it. Run
`xattr -d com.apple.quarantine ict` or approve it in System Settings > Privacy & Security.

The jar is also published to Maven Central as `org.ical4j:ict`, for running on a JVM or embedding.

## Usage

Commands are verb first: `ict <verb> <type>`. `ict --help` lists the verbs, `ict <verb> --help`
lists the types under a verb, and `ict <verb> <type> --help` lists that operation's options.

```sh
# Collections live in a workspace
ict add collection --collection work
ict add collection --collection people --type card --description "Contacts"
ict list collection

# Calendar data moves in and out of a collection with import/export
ict import calendar --collection work --file meetings.ics
curl -s https://example.com/holidays.ics | ict import calendar --collection work
ict list event --collection work
ict export calendar --collection work --uid 1234@example.com > meeting.ics
ict export calendar --collection work --format json        # jCal

# Cards work the same way
ict import card --collection people --file contacts.vcf
ict list entity --collection people
```

### Operations

| Command | MCP tool | Does |
|---|---|---|
| `ict add collection --collection N [--type calendar\|card] [--description D]` | `add_collection` | Add a collection to the workspace |
| `ict get collection --collection N [--type T]` | `get_collection` | Describe a collection |
| `ict list collection` | `list_collection` | List the workspace's collections |
| `ict remove collection --collection N [--type T]` | `remove_collection` | Remove a collection and its contents |
| `ict import calendar --collection N [--file F]` | `import_calendar` | Import iCalendar data, merging by UID (stdin if no file) |
| `ict export calendar --collection N [--uid U]` | `export_calendar` | Export one object, or the whole collection, with its VTIMEZONEs |
| `ict remove calendar --collection N --uid U` | `remove_calendar` | Remove one calendar object |
| `ict import card --collection N [--file F]` | `import_card` | Import vCard data, merging by UID (stdin if no file) |
| `ict export card --collection N [--uid U]` | `export_card` | Export one card, or the whole collection |
| `ict remove card --collection N --uid U` | `remove_card` | Remove one card |
| `ict list event --collection N` | `list_event` | List events (VEVENT) |
| `ict list action --collection N` | `list_action` | List actions (VTODO) |
| `ict list note --collection N` | `list_note` | List notes (VJOURNAL) |
| `ict list entity --collection N` | `list_entity` | List contacts (cards) |

The words come from a fixed vocabulary of verbs (`list`, `get`, `add`, `remove`, `import`,
`export`, and so on) and types (`event`, `action`, `note`, `entity`, `calendar`, `card`,
`collection`, ...). More operations are admitted as they gain tests; see
[`docs/command-protocols.md`](docs/command-protocols.md).

### Global options

These work on every command, before or after the verb.

| Option | Effect |
|---|---|
| `--workspace W` | Act in workspace `W` instead of the active one from configuration |
| `--format text\|json\|xml` | Output format, `text` by default (see below) |
| `-v`, `-vv`, `-vvv` | Log at info, debug or trace level, to stderr |
| `-q`, `--quiet` | Log errors only |
| `--lenient` | Relax iCalendar parsing and unfolding |
| `-h`, `--help` / `-V`, `--version` | Help and version |

### Output formats

| Result | `text` (default) | `json` | `xml` |
|---|---|---|---|
| Calendar data | iCalendar (RFC 5545) | jCal (RFC 7265) | xCal (RFC 6321) |
| Card data | vCard (RFC 6350) | jCard (RFC 7095) | xCard (RFC 6351) |
| Lists of items | aligned columns with a header | array of objects | not available |
| Descriptions and status | `name: value` lines | object | not available |

Results go to stdout and nothing else does: diagnostics and logs go to stderr, so `ict` output
can be piped safely.

### Exit codes

| Code | Meaning |
|---|---|
| 0 | Success |
| 1 | The operation failed: not found, already exists, invalid data, or a store error |
| 2 | Usage error: unknown command or option, missing required option |

### Configuration

Configuration and the local stores live in the platform data directory:
`~/Library/Application Support/iCal4j` on macOS, `~/.local/share/iCal4j` on Linux and
`%APPDATA%\iCal4j` on Windows. Set `ICT_DATA_DIR` to use another directory.

```sh
ict config                        # list every setting
ict config workspace.active       # show one
ict config workspace.active home  # change one
```

| Environment variable | Effect |
|---|---|
| `ICT_DATA_DIR` | Data directory, in place of the platform default |
| `ICT_LOG_LEVEL` | Log level (`trace`, `debug`, `info`, `warn`, `error`, `off`); overrides `-v` and `-q` |

## MCP server

`ict mcp` runs a Model Context Protocol server on stdio. It exposes the operations above as tools
named `<verb>_<type>`, with input schemas derived from the same option definitions, and runs until
the client closes its input. Calendars and cards come back as iCalendar or vCard text, lists and
descriptions as JSON. A failed operation (a missing collection, say) comes back as a tool result
with `isError` set, and the server keeps running.

To use it from an MCP client, register the executable with the `mcp` argument. For Claude Code:

```sh
claude mcp add ict -- /path/to/ict mcp
```

For Claude Desktop and other clients that use a JSON configuration:

```json
{
  "mcpServers": {
    "ict": {
      "command": "/path/to/ict",
      "args": ["mcp"],
      "env": { "ICT_DATA_DIR": "/path/to/a/data/directory" }
    }
  }
}
```

`ICT_DATA_DIR` is optional; set it to give the assistant its own store, separate from the one
you use in the terminal. Add `"--workspace", "<name>"` to `args` to pin a workspace. While the
server runs, stdout carries only protocol messages; logs (add `-v` to `args` to see more) go to
stderr, which most clients write to their MCP log.

Tool handlers cannot read the protocol's stdin, so `import_calendar` and `import_card` need the
`file` argument when called over MCP.

## Building from source

The JVM build needs JDK 17; the native build needs GraalVM CE for JDK 25 or later.

```sh
./gradlew build              # compile and run the specs
./gradlew installDist        # JVM launcher at build/install/ict/bin/ict
./gradlew jvmSmoke           # smoke suite against the JVM build

export GRAALVM_HOME=/path/to/graalvm-25
JAVA_HOME=$GRAALVM_HOME ./gradlew nativeCompile --no-configuration-cache   # build/native/nativeCompile/ict
./gradlew nativeSmoke nativeStartup -PictBinary=build/native/nativeCompile/ict
```

`make native` and `make smoke` wrap the last two lines. Native reachability metadata is checked in
under `src/main/resources/META-INF/native-image/org.ical4j/ict`; after adding an operation, run
`./gradlew mergeTracedMetadata` (with `GRAALVM_HOME` set) to capture what it needs from a smoke run
under the tracing agent, and review the diff. See
[`docs/command-protocols.md`](docs/command-protocols.md) for how operations are defined and admitted.

On macOS 27, if native-image fails with `libSystem.B.tbd: unknown architecture`, the Command Line
Tools' clang does not yet understand the macOS 27 SDK. Point the build at an older SDK, for
example `SDKROOT=/Library/Developer/CommandLineTools/SDKs/MacOSX26.5.sdk`, or update the tools.

## License

[iCal4j License](https://raw.githubusercontent.com/ical4j/ical4j/master/LICENSE) (BSD-3-Clause).
