## ADDED Requirements

### Requirement: Every operation is defined exactly once in the registry
The system SHALL hold a single registry of operations. Each operation SHALL be identified by its (verb, type, locus) tuple from the command vocabulary and SHALL carry: an ordered list of parameter descriptors, a handler, a result type, and a per-surface exposure flag. The registry MUST reject a second registration of the same tuple.

#### Scenario: Registering an operation
- **WHEN** the operation (`list`, `event`, `collection`) is registered with a `collection` parameter and a handler
- **THEN** the registry exposes exactly one entry for that tuple with that parameter list

#### Scenario: Duplicate tuple is rejected
- **WHEN** a second operation with the tuple (`list`, `event`, `collection`) is registered
- **THEN** registration fails at startup with an error naming the tuple

### Requirement: Parameter descriptors are surface-neutral
Each parameter descriptor SHALL declare a name, a value type (string, integer, boolean, enum, path, or a list thereof), whether it is required, a default, and a description. Descriptors MUST NOT reference picocli, MCP or JSON schema types directly; each surface derives its own representation.

#### Scenario: Deriving a CLI option
- **WHEN** an operation declares an optional string parameter `collection` with description "Collection name"
- **THEN** the CLI view exposes `--collection <name>` with that description

#### Scenario: Deriving an MCP input schema
- **WHEN** the same operation is exposed to MCP
- **THEN** the tool's input schema declares a `collection` property of type `string` with that description and it is absent from the schema's `required` list

### Requirement: Handlers return structured results, not formatted text
A handler SHALL return a typed result (a calendar, a card, a list of identifiers, a collection descriptor, or a status record). A handler MUST NOT write to stdout or stderr and MUST NOT return pre-formatted text intended for a specific surface.

#### Scenario: List handler returns identifiers
- **WHEN** the (`list`, `event`, `collection`) handler runs against a collection containing two events
- **THEN** it returns a list of two identifiers and writes nothing to any stream

#### Scenario: Failure is a typed error
- **WHEN** a handler is invoked for a collection that does not exist
- **THEN** it throws or returns a typed not-found error, and each surface maps it to its own convention (a non-zero exit code, an MCP error result)

### Requirement: Handlers receive a store through a context, never via a global
A handler SHALL obtain object stores, the active workspace and configuration from a context argument supplied by the surface. Handlers MUST NOT read the static configuration singleton directly.

#### Scenario: Test injects a store
- **WHEN** a Spock spec invokes a handler with a context holding an in-memory store
- **THEN** the handler operates on that store and touches no files under the user's data directory

### Requirement: Surfaces do not call each other
The CLI view and the MCP view SHALL each invoke operations only through the registry. The MCP view MUST NOT invoke CLI command classes and the CLI view MUST NOT invoke MCP tool classes.

#### Scenario: MCP executes a list
- **WHEN** an MCP client calls the `list_event` tool
- **THEN** the MCP view resolves (`list`, `event`, `collection`) from the registry and invokes its handler directly

### Requirement: Admission requires a test and a native smoke run
An operation SHALL be added to the registry only together with a Spock specification exercising its handler against an injected store, and its inclusion in the native smoke test suite. The build MUST fail if a registered operation has no corresponding specification.

#### Scenario: Operation without a spec
- **WHEN** an operation is registered and no specification references its tuple
- **THEN** the registry conformance test fails naming the tuple

#### Scenario: First cohort
- **WHEN** the registry is first populated
- **THEN** it contains exactly the operations covered by the existing green specifications: add/get/remove collection, list collections, import/export/remove calendar, import/export/remove card, and list within a collection
