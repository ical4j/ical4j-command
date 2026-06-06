## Why

The same operations are described three times, in three idioms, across the CLI, MCP, and agent surfaces — and the names already drift even within a single file (e.g. `getEvents` is named `get` but *lists*; `createCollection` vs `addCalendar`; `delete` vs `remove`). Without a shared, controlled vocabulary, every new surface re-invents names and the three surfaces diverge further over time.

## What Changes

- Introduce a **controlled vocabulary** of canonical **verbs**, **entity types**, and **loci** (containers) that every surface — CLI, MCP, and agent — names operations from.
- Adopt **Option B**: surfaces share the *vocabulary* (names + taxonomy) but keep their *own dispatch*. There is no shared command-execution core; conformance is by naming rule, not by a common dispatcher.
- Fix the established naming axes:
  - `list` (many) vs `get` (one-by-id) — never `get` for plural results.
  - Standardise on `add` / `remove` (collapsing `create`→`add` and `delete`→`remove`); synonyms are not exposed.
  - Treat `publish` / `request` / `reply` as specialised `send` operations (iTIP methods), not independent verbs.
- Define the **locus hierarchy** as exactly two levels: `workspace ⊃ collection`. Entities (event, action, issue, note, observance, availability, report, entity/contact, resource) live **directly in a collection**.
- Classify `calendar` and `card` as **serialization/object families** (iCalendar / vCard), not loci and not entity types.
- Introduce a new **shared core module** that holds the vocabulary (constants/enums) and is depended on by CLI, MCP, and agent — vocabulary only, no dispatch.

## Capabilities

### New Capabilities
- `command-vocabulary`: The canonical verbs, entity types, and loci, plus the naming rules each surface (CLI, MCP, agent) must conform to.

### Modified Capabilities
<!-- None: no existing specs to modify. -->

## Impact

- **New core module**: a vocabulary-only module (constants/enums) added to `settings.gradle`, depended on by the three surfaces.
- **CLI** (`ical4j-command-cli`): the in-flight verb-first top-level commands (`copy`, `move`, …) and noun-grouped commands must align to canonical names; `delete` aligns to `remove`.
- **MCP** (`ical4j-command-mcp`): `@Tool` method names (e.g. `getEvents` → list semantics, `addCalendar`/`removeCalendar`) must align.
- **Agent** (`ical4j-command-agent`): `@Tool` names (`generateEvent`, `generateMeeting`) map to canonical verb/type.
- No runtime coupling introduced; the commented `new ListCommand(...)` reach-through in `WorkspaceService` stays removed under Option B.
