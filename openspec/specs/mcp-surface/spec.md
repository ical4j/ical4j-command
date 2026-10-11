# mcp-surface Specification

## Purpose
The `ict mcp` server: the operation registry served as Model Context Protocol tools over stdio,
with input schemas derived from parameter descriptors and stdout reserved for the protocol.
## Requirements
### Requirement: `ict mcp` serves the registry over stdio
The `mcp` subcommand SHALL start a Model Context Protocol server on the stdio transport and SHALL block until the client closes the stream. It SHALL be implemented with the MCP Java SDK using programmatic tool registration, not annotation scanning.

#### Scenario: Initialize handshake
- **WHEN** a client writes an `initialize` request to the process's stdin
- **THEN** the process answers with server name `ict`, the build version, and a `tools` capability

#### Scenario: Clean shutdown
- **WHEN** the client closes stdin
- **THEN** the process exits 0 without writing further bytes to stdout

### Requirement: Tools are derived from the registry
Each registry operation flagged for MCP exposure SHALL become one tool named `<verb>_<type>` with an input schema generated from the operation's parameter descriptors and a description taken from the operation. There MUST NOT be a hand-written tool per operation.

#### Scenario: Tool listing
- **WHEN** a client calls `tools/list`
- **THEN** the response includes `list_event` with a `collection` string property, and no tool exists for an operation not flagged for MCP

#### Scenario: Tool call maps to the handler
- **WHEN** a client calls `list_event` with `{"collection":"work"}`
- **THEN** the (`list`, `event`, `collection`) handler runs and its result is returned as content

### Requirement: Results render as MCP content
Calendars and cards SHALL be returned as text content in iCalendar or vCard form. Lists and descriptors SHALL be returned as JSON text content. Handler errors SHALL be returned as tool results with `isError` set, not as protocol errors.

#### Scenario: Not found via MCP
- **WHEN** `get_collection` is called for a collection that does not exist
- **THEN** the tool result has `isError` true and a message naming the collection, and the server keeps running

### Requirement: stdout is reserved for the protocol
While `ict mcp` runs, nothing other than the MCP transport SHALL write to stdout. Logging, picocli output and library warnings SHALL be routed to stderr. The build SHALL include a test that runs the server with verbose logging enabled and asserts every stdout line is a valid JSON-RPC message.

#### Scenario: Verbose logging during MCP
- **WHEN** `ict mcp --verbose` handles a `tools/call` that logs at debug level
- **THEN** the log line appears on stderr and stdout contains only JSON-RPC frames

