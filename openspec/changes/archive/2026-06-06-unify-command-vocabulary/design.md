# Design — unify-command-vocabulary

## Context

The same operations appear across three surfaces, each organized around a different primary axis, which causes naming drift:

| Surface | Organizes around | Result |
|---------|-------------------|--------|
| CLI (old) | type (noun groups) | `calendar delete`, `strategy create-event` |
| CLI (new) | verb (top-level) | `copy`, `move`, `delete` — type becomes an arg |
| MCP | verb×type flat matrix | `getEvents`, `addCalendar`, `createCollection` |
| Agent | task (fuzzy) | `generateMeeting`, `publish/request/reply` |

Drift is already observable inside a single file (`CollectionService`): `getActions` is named `get` but returns a list; `createCollection` vs `addCalendar`; `deleteCollection` vs `removeCalendar`.

## Decisions

### D1 — Option B: shared vocabulary, separate dispatch
Surfaces agree on **names and taxonomy**, not on a shared execution core.

- **Why:** Less invasive to the existing picocli tree and the Spring-AI MCP services; no need to find one abstraction that fits picocli *and* MCP *and* `@Tool` dispatch.
- **Trade-off accepted:** conformance is by convention/naming rule, with drift risk, rather than enforced by a single dispatcher.
- **Consequence:** the commented `new ListCommand(...)` reach-through in `WorkspaceService.java:48` (MCP → CLI) stays removed. That coupling was an Option-A artifact and is out of scope.

### D2 — Locus is a real third axis, two levels deep
Operation identity is `(verb, type, locus)`. Locus is a containment hierarchy: `workspace ⊃ collection`. Entities live **directly in a collection**.

- **Why:** `copy event between collections` only makes sense if locus is orthogonal to type. Keeping it to two levels caps model complexity.
- **Rejected alternative:** `collection ⊃ calendar ⊃ event` (a three-level hierarchy where calendar is a container). Rejected per D3.

### D3 — calendar / card are serialization families, not loci or types
`calendar` (iCalendar) and `card` (vCard) are the import/export representations, not containers and not entity types.

- **Why:** A collection groups events, notes, and contacts together (per README use cases). Events do not live "inside a calendar inside a collection" — the calendar is just how that collection's temporal entities serialize. Treating calendar/card as a locus level would have forced a third hierarchy level (D2) and split entities by format rather than by collection.
- **Consequence:** moving an `.ics`/`.vcf` in or out of a collection uses `import`/`export`, not `create`/`add` of a "calendar" container.

### D4 — Verb canonicalization: standardise on add/remove
`list` (many) vs `get` (one). The `create`/`add` pair collapses to **`add`** (place into a locus, new or existing); the `delete`/`remove` pair collapses to **`remove`** (take out of a locus). Synonyms (`create`, `delete`, `fetch`, `destroy`, …) map to canonical verbs and are not exposed.

- **Why:** the create/add and delete/remove distinctions added vocabulary without earning their keep across all three surfaces, and `add`/`remove` already match the CLI's existing `AddCollection` / `RemoveCollection` commands.

### D5 — The vocabulary lives in a new shared core module
The canonical verbs/types/loci become constants/enums in a **new core module** that CLI, MCP, and agent all depend on.

- **Why:** a docs-only convention drifts; a shared compile-time definition makes conformance cheap to follow and visible to refactoring tools.
- **Boundary:** the core module holds *vocabulary only* — no dispatch or execution. This keeps Option B intact (shared names, separate dispatch) rather than sliding into Option A.

### D6 — iTIP methods are specialised sends
`publish` / `request` / `reply` are the same axis as `send` — a `send` carrying a specific iTIP method — not independent verbs.

- **Why:** they differ by the iTIP method on the message, not by the action; collapsing them keeps the verb axis small and reflects that `CalendarUserAgent` is really emitting calendar messages over a channel.

### D7 — A `generate` verb for strategy/template synthesis (revealed during apply)
Implementation surfaced a command family the vocabulary did not cover: the CLI's strategy generators (`CreateEvent`, `CreateAction`, …, `CreateCalendar`, `CreateCard`) *synthesise* objects from templates rather than placing existing items into a locus. `add` ("place into a locus") does not fit, and the `add` name is already owned by the metadata command group.

- **Decision:** add `generate` to the canonical verb set. The strategy group is named `generate` (alias `new`); the metadata group keeps `add`. Generation is locus-independent; placing a generated item into a collection is a separate `add`.

## Open questions

- (none currently — D1–D7 resolve the forks surfaced during exploration and apply)
