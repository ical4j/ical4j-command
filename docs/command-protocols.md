# Command protocols

`ict` has one table of operations, the **registry**, and two **views** over it: the command line
and the MCP server. An operation is defined once, with its parameters and handler; each view
derives its own interface from that definition and executes through the registry. Nothing is
written per surface.

```
                    ┌──────────────────────────────┐
  ict <verb> <type> │  cli.CommandTree             │
  ────────────────► │  picocli subcommands/options │──┐
                    └──────────────────────────────┘  │   ┌────────────────────────┐
                                                      ├──►│ ops.Registry           │──► handler(Context, Params)
                    ┌──────────────────────────────┐  │   │ (verb, subject, locus) │        │
  tools/call        │  mcp.Tools                   │──┘   └────────────────────────┘        ▼
  ────────────────► │  tools + JSON input schemas  │                                     Result
                    └──────────────────────────────┘                                        │
                                                       ops.Renderer: text / json / xml  ◄───┘
```

## Vocabulary

Operations are named from a controlled vocabulary in `org.ical4j.command.vocab`. The package
holds definitions only; it has no dispatch logic.

- **Verbs** (`Verb`): `list` (many), `get` (one), `generate` (synthesise from a template), `add`
  (place into a locus), `update`, `remove` (take out of a locus), `copy`, `move`, `filter`,
  `validate`, `import`, `export`, `send`, `receive`. There is no `create` or `delete`; those
  are `add` and `remove`.
- **Entity types** (`EntityType`): `event`, `action`, `issue`, `note`, `observance`,
  `availability`, `report`, `entity` (a contact), `resource`. All live directly in a collection.
- **Serialization families** (`SerializationFamily`): `calendar` (iCalendar) and `card` (vCard),
  the representations that `import` and `export` move across a collection boundary.
- **Loci** (`Locus`): `workspace` contains `collection`s, which contain entities; the hierarchy
  is two levels deep. `channel` is the locus of `send` and `receive`, and `none` marks
  operations on a stream or file that touch no store, such as `generate`.
- **iTIP methods** (`SendMethod`): `publish`, `request` and `reply` qualify `send`; they are not
  verbs.

The type axis of an operation is a `Subject`: an entity type, a serialization family, or a
container locus when the container itself is the object (`list collection in workspace`,
`add collection in workspace`). `channel` and `none` are never subjects.

## Operations

An operation is an `ops.Operation`:

| Part | Type | Notes |
|---|---|---|
| identity | `OperationId(verb, subject, locus)` | written `list event in collection`, or `generate event` when the locus is `none` |
| description | `String` | one sentence; becomes CLI help and the MCP tool description |
| parameters | `List<Param>` | name, type (`string`, `integer`, `boolean`, `enum`, `path`, or a list of one), required flag, default, description |
| exposure | `Set<Surface>` | `CLI`, `MCP`, or both (the default) |
| handler | `(Context, Params) -> Result` | the implementation |

Handlers follow three rules:

- **They return structure, not text.** A `Result` is a calendar, a card, identifiers, one or more
  descriptors (name/value attributes), a status message or a validation report. `ops.Renderer`
  turns it into text, JSON or XML for whichever surface asked.
- **They signal failure with a typed `OperationException`**: `NotFound`, `AlreadyExists`,
  `Usage`, `InvalidData` or `Store`. Each surface maps these to its own convention.
- **They touch the world only through their `Context`**: the active workspace, the calendar and
  card stores, and standard input. Surfaces build the context from configuration; specs build it
  around a temp-dir store. Handlers never read configuration or write to a stream.

The `Registry` rejects a second operation with the same identity, and `Operations.standard()`
assembles the operations the binary ships.

## The CLI view

`cli.CommandTree` builds the picocli tree from the registry at startup, using picocli's
programmatic API so no command class is annotated or reflected on.

- One subcommand per verb that has CLI-exposed operations, in vocabulary order, and under each
  one subcommand per operation, named by its subject: `ict list event`.
- Every parameter becomes an option, `--<name>`, with the parameter's description, required
  flag, choices and default in its help. Values reach the handler as strings and are converted
  there, so no JDK type is ever registered for reflection.
- Loci are options. `--collection` is the shared `collection` parameter that every
  collection-locus operation declares, so it appears only where it applies. `--workspace` is a
  global option that overrides the configured active workspace.
- Global options (`--workspace`, `--format`, `-v`, `-q`, `--lenient`, `--help`, `--version`)
  are inherited by every command. A parameter may not reuse one of their names.
- The CLI names an operation by verb and subject only, so two operations differing only in
  locus cannot both be exposed to it; the tree builder fails at startup if they are.

Results are rendered in the `--format` requested and written to stdout. Failures write
`ict: <message>` to stderr and exit 1, or 2 for usage errors (including picocli's own parse
errors and `OperationException.Usage`). Unexpected exceptions also print a stack trace.

`ict config` is the one hand-written command. It edits the configuration the context is built
from, so it is not an operation.

## The MCP view

`mcp.Tools` turns each MCP-exposed operation into a tool and `mcp.McpCommand` serves them with
the MCP Java SDK's synchronous server on the stdio transport (`ict mcp`).

- Tool names are `<verb>_<subject>`: `list_event`, `import_calendar`. As with the CLI, a name
  clash fails at startup.
- The input schema is a JSON schema object generated from the parameters: `string`, `integer`
  and `boolean` map directly, `enum` becomes a string with `enum` values, `path` a string, and
  list parameters arrays. Required parameters are listed in `required`, defaults appear as
  `default`, and unknown properties are rejected.
- Tools carry `readOnlyHint` for `list`, `get`, `export`, `validate` and `filter`, and
  `destructiveHint` for `remove`, `move` and `update`.
- Calendars and cards are returned as iCalendar or vCard text; everything else as JSON text.
  An `OperationException`, or any other failure, is returned as a tool result with `isError`
  set and the message as its text. Protocol errors are left to the SDK.
- The server's stdout carries protocol messages only. `System.out` and picocli's output are
  pointed at stderr while it runs, logging goes to stderr already, and handlers get an empty
  stdin. A spec runs the server with `-vvv` and checks every stdout line is JSON-RPC.

## Adding an operation

An operation enters the registry only with tests and a smoke run (design D8 of the
`single-native-binary` change):

1. Define it in the `ops` class for its subject and add it to `Operations.standard()`.
2. Write a Spock specification that runs its handler against a temp-dir store and names its
   identity as a literal, for example `op('list event in collection')`.
   `RegistryConformanceTest` fails the build for any registered operation that no
   specification names.
3. Add a step to `src/smoke/.../SmokeSuite.java` that invokes it, marked with the identity it
   covers. The suite, and `SmokeSuiteTest`, fail for any operation without a step.
4. Run `./gradlew mergeTracedMetadata` with `GRAALVM_HOME` set. This runs the smoke suite on the
   JVM under the native-image tracing agent and merges what it records into the checked-in
   reachability metadata. Review the diff.
5. Check the native binary: `./gradlew nativeSmoke`.

Nothing is needed in `cli` or `mcp`: the command, its options, the tool and its schema all
follow from the definition. The commands awaiting admission, with the identity each is
expected to take, are listed in [`dropped-commands.md`](dropped-commands.md).
