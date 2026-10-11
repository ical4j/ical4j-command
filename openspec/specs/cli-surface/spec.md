# cli-surface Specification

## Purpose
The `ict` command line: a verb-first picocli tree generated from the operation registry at
startup, with global options, per-format rendering, exit codes and stderr-only diagnostics.
## Requirements
### Requirement: Command tree is verb-first
The `ict` executable SHALL expose each registered operation as `ict <verb> <type>` where `verb` and `type` are the lowercase vocabulary names. Loci SHALL be supplied as options (`--workspace`, `--collection`), never as positionals. Operations whose locus is `none` (for example `generate`) SHALL accept no locus option.

#### Scenario: Listing events in a collection
- **WHEN** the user runs `ict list event --collection work`
- **THEN** the (`list`, `event`, `collection`) operation runs against the collection named `work` in the active workspace

#### Scenario: Locus option missing falls back to configuration
- **WHEN** the user runs `ict list collection` without `--workspace`
- **THEN** the active workspace from configuration is used

#### Scenario: Help groups by verb
- **WHEN** the user runs `ict --help`
- **THEN** the usage lists one subcommand per verb, and `ict list --help` lists the types available under `list`

### Requirement: The CLI is generated from the registry
The picocli command tree SHALL be built from the operation registry at startup. There MUST NOT be a hand-written picocli command class per operation.

#### Scenario: New operation appears without CLI code
- **WHEN** an operation for (`validate`, `event`, `collection`) is added to the registry
- **THEN** `ict validate event` exists with options derived from its parameter descriptors and no CLI-specific class was added

### Requirement: Results render per output format
The CLI SHALL render structured results to stdout. Calendars and cards SHALL render as iCalendar or vCard text by default. Lists and descriptors SHALL render as plain lines by default. A global `--format` option SHALL select `text`, `json` or, where the serializer supports it, `xml`.

#### Scenario: Default calendar output
- **WHEN** `ict export calendar --collection work` succeeds
- **THEN** stdout contains an iCalendar stream beginning with `BEGIN:VCALENDAR`

#### Scenario: JSON list output
- **WHEN** `ict list collection --format json` succeeds
- **THEN** stdout contains a JSON array of collection descriptors and nothing else

### Requirement: Exit codes and diagnostics
The CLI SHALL exit 0 on success, 1 on an operation failure (not found, validation failure, store error), and 2 on usage errors. Diagnostics and logs SHALL go to stderr only.

#### Scenario: Not found
- **WHEN** `ict get collection --collection missing` is run and no such collection exists
- **THEN** the process exits 1, stderr names the collection, and stdout is empty

#### Scenario: Usage error
- **WHEN** `ict list` is run with no type
- **THEN** the process exits 2 and stderr shows the usage for `list`

### Requirement: Version and configuration commands
`ict --version` SHALL print the release version derived from the build. `ict config` SHALL read and write the TOML configuration in the user's data directory as today.

#### Scenario: Version
- **WHEN** the user runs `ict --version`
- **THEN** stdout contains the version string embedded at build time

