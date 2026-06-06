# command-vocabulary Specification

## Purpose

Define a single controlled vocabulary — canonical verbs, entity types, and loci — that all
command surfaces (CLI, MCP, agent) name operations from. Surfaces share these names but
dispatch independently (Option B: shared names, separate dispatch). The vocabulary is
defined in the `ical4j-command-core` module.

## Requirements

### Requirement: Operation identity is (verb, type, locus)
Every operation exposed by any surface (CLI, MCP, agent) SHALL be identified by a tuple of exactly three axes: a **verb** (the action), an **entity type** (what is acted on), and a **locus** (the container the action acts within). Surface-specific naming SHALL be derivable from this tuple.

#### Scenario: Decomposing an existing operation
- **WHEN** the MCP method `listCollections(workspace)` is mapped to the vocabulary
- **THEN** it resolves to verb `list`, type `collection`, locus `workspace`

#### Scenario: Two surfaces name the same operation
- **WHEN** the CLI and the agent both expose "list the events in a collection"
- **THEN** both resolve to the same tuple (`list`, `event`, `collection`) even though their surface syntax differs

### Requirement: Canonical verb vocabulary
The system SHALL define a closed set of canonical verbs. Surfaces MUST use these verbs and MUST NOT introduce synonyms for an existing verb. The verbs and their meanings are:

- `list` — return many items of a type within a locus.
- `get` — return one item of a type, addressed by identifier.
- `generate` — synthesise a new item from a strategy/template (e.g. an event from a meeting template), independent of any locus.
- `add` — place an item into a locus, whether newly made or pre-existing.
- `update` — modify an existing item in place.
- `remove` — take an item out of a locus.
- `copy` — duplicate an item into a (possibly different) locus.
- `move` — relocate an item from one locus to another.
- `filter` — derive a subset of a type within a locus by expression.
- `validate` — check an item or locus for conformance.
- `import` / `export` — read/write a serialization family (calendar/card) into/out of a locus.
- `send` / `receive` — transmit/accept items over a channel.

#### Scenario: Plural results never use `get`
- **WHEN** an operation returns more than one item
- **THEN** its verb is `list` (or `filter`), never `get`

#### Scenario: Add covers both new and existing items
- **WHEN** an operation places an item into a locus, whether it is brand-new or already exists
- **THEN** its verb is `add` — there is no separate `create` verb

#### Scenario: Generate is distinct from add
- **WHEN** an operation synthesises an item from a strategy/template without placing it into a locus (e.g. emitting a generated event to output)
- **THEN** its verb is `generate`, not `add`
- **WHEN** the generated item is subsequently placed into a locus
- **THEN** that placement is a separate `add` operation

#### Scenario: Remove covers both detach and destroy
- **WHEN** an operation takes an item out of a locus, whether the item continues to exist elsewhere or not
- **THEN** its verb is `remove` — there is no separate `delete` verb

#### Scenario: Rejecting a synonym
- **WHEN** a surface proposes an operation named with a verb synonym (e.g. `fetch`, `make`, `create`, `delete`, `destroy`)
- **THEN** it is mapped to the canonical verb (`get`, `add`, `remove`) and the synonym is not exposed

### Requirement: Canonical entity types
The system SHALL define the set of entity types that operations act on: `event`, `action`, `issue`, `note`, `observance`, `availability`, `report`, `entity` (contact), and `resource`. Surfaces MUST name types from this set.

#### Scenario: Naming an operation by type
- **WHEN** an operation lists actions in a collection
- **THEN** the type axis is `action` and the operation resolves to (`list`, `action`, `collection`)

### Requirement: Locus hierarchy has exactly two levels
The system SHALL define loci as a containment hierarchy of exactly two levels: a `workspace` contains `collection`s, and a `collection` directly contains entities. No additional container level exists between a collection and its entities.

#### Scenario: Entities live directly in a collection
- **WHEN** an event is created within a collection
- **THEN** its locus is `collection` directly, with no intervening container

#### Scenario: Collections live in a workspace
- **WHEN** a collection is created
- **THEN** its locus is `workspace`

### Requirement: Calendar and card are serialization families, not loci or types
The system SHALL treat `calendar` (iCalendar) and `card` (vCard) as serialization/object families used for import and export, NOT as loci and NOT as entity types. Operations that move data between a serialization family and a collection SHALL use the `import`/`export` verbs.

#### Scenario: Importing a calendar populates a collection
- **WHEN** a `.ics` calendar is brought into a collection
- **THEN** the operation is `import` with the `calendar` family, targeting locus `collection` — not an `add` of a `calendar` container

#### Scenario: Calendar is not a container level
- **WHEN** resolving the locus of an event
- **THEN** the locus is `collection`, and `calendar` does not appear as a container in the locus hierarchy

### Requirement: iTIP methods are specialised send operations
The system SHALL treat the calendar messaging methods `publish`, `request`, and `reply` as specialised forms of the `send` verb (a `send` carrying a specific iTIP method), not as independent verbs on the verb axis.

#### Scenario: Publish resolves to a specialised send
- **WHEN** the agent's `CalendarUserAgent.publish(...)` is mapped to the vocabulary
- **THEN** it resolves to `send` specialised with the iTIP method `PUBLISH`, on locus `channel`

#### Scenario: Request and reply share the send axis
- **WHEN** `request` and `reply` are mapped to the vocabulary
- **THEN** both resolve to `send` specialised with their respective iTIP methods, and neither introduces a new top-level verb

### Requirement: Surfaces share vocabulary but dispatch independently
Each surface (CLI, MCP, agent) SHALL name its operations from the shared vocabulary, but SHALL retain its own dispatch and execution. There SHALL NOT be a shared command-execution core that surfaces delegate into; conformance is enforced by naming rules, not by a common dispatcher.

#### Scenario: No cross-surface execution coupling
- **WHEN** the MCP layer exposes a `list` operation
- **THEN** it implements its own dispatch and does not call the CLI's command classes to execute the operation

#### Scenario: Adding a verb to the vocabulary
- **WHEN** a new canonical verb is added to the vocabulary
- **THEN** each surface may adopt it independently using the shared name, with no shared dispatcher required to change

### Requirement: The vocabulary lives in a shared core module
The canonical verbs, entity types, and loci SHALL be defined as constants/enums in a new shared **core module**. Each surface module (CLI, MCP, agent) SHALL depend on the core module for these names. The core module SHALL contain vocabulary definitions only — it MUST NOT contain command dispatch or execution logic, preserving Option B (shared names, separate dispatch).

#### Scenario: Surfaces reference core vocabulary constants
- **WHEN** a surface names an operation
- **THEN** it derives the verb, type, and locus names from the core module's definitions rather than declaring its own string literals

#### Scenario: Core module carries no dispatch
- **WHEN** the core module is inspected
- **THEN** it contains only vocabulary definitions (verbs, types, loci) and no command-execution or dispatch code
