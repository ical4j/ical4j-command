## 1. Core vocabulary module

- [x] 1.1 Create new Gradle module (e.g. `ical4j-command-core`) and register it in `settings.gradle`
- [x] 1.2 Define `Verb` enum: `LIST`, `GET`, `ADD`, `UPDATE`, `REMOVE`, `COPY`, `MOVE`, `FILTER`, `VALIDATE`, `IMPORT`, `EXPORT`, `SEND`, `RECEIVE`
- [x] 1.3 Define `EntityType` enum: `EVENT`, `ACTION`, `ISSUE`, `NOTE`, `OBSERVANCE`, `AVAILABILITY`, `REPORT`, `ENTITY`, `RESOURCE`
- [x] 1.4 Define `Locus` enum: `WORKSPACE`, `COLLECTION` (two levels only)
- [x] 1.5 Define `SerializationFamily` enum: `CALENDAR`, `CARD` (not loci, not types)
- [x] 1.6 Model iTIP methods (`PUBLISH`, `REQUEST`, `REPLY`) as a specialisation of `SEND` (e.g. `SendMethod` carried alongside `Verb.SEND`)
- [x] 1.7 Confirm the core module has NO dispatch/execution dependencies (vocabulary only)

## 2. CLI conformance (`ical4j-command-cli`)

- [x] 2.1 Add dependency on the core module
- [x] 2.2 Rename/align `delete`-named commands to `remove` (keep `cp`/`rm`-style aliases for usability)
- [x] 2.3 Align `create`/`new`-named commands to `generate` (strategy group `new`→`generate`; `create-calendar`/`create-card`→`calendar`/`card`). Note: `add` reserved for metadata group; added `generate` verb (D7)
- [x] 2.4 Audit `list` vs `get`: no violations — `list-*` return many, `get-*` return one; `collections` is a group namespace, not an operation
- [x] 2.5 Reframe calendar/card CRUD as import/export: `get-calendar`→`export-calendar`, `update-calendar`→`import-calendar` (+ card equivalents); `remove-*` unchanged
- [x] 2.6 Channel commands already use `send`/`receive` (compliant); `publish`/`request`/`reply` are agent-side (see 4.3)

## 3. MCP conformance (`ical4j-command-mcp`)

- [x] 3.1 Add dependency on the core module
- [x] 3.2 Rename `getEvents`/`getActions`/… (list semantics) to `list*` (9 methods in CollectionService)
- [x] 3.3 Rename `createCollection`→`addCollection`, `deleteCollection`→`removeCollection` (also `createWorkspace`/`deleteWorkspace`→`add`/`remove` in StoreService for consistency)
- [x] 3.4 Reframe `addCalendar`/`addCard`→`importCalendar`/`importCard`; `removeCalendar`/`removeCard` already use `remove`
- [x] 3.5 Reconcile `getCollectionInfo`/`getCalendarInfo`/`getCardInfo` to canonical `getCollection`/`getCalendar`/`getCard`
- [x] 3.6 Remove the dead `new ListCommand(...)` reach-through in `WorkspaceService` (Option B: no cross-surface dispatch)

## 4. Agent conformance (`ical4j-command-agent`)

- [x] 4.1 Add dependency on the core module (required enabling the module in `settings.gradle`)
- [x] 4.2 `@Tool` `generateEvent`/`generateMeeting`/`generateAppointment` map to the `generate` verb (D7); `EntityBuilder.newIndividual`→`generateIndividual`, `addEmail` already uses `add`
- [x] 4.3 `CalendarUserAgent.publish/request/reply` align to specialised `send` semantics (D6) — names already correct, no code change

## 5. Documentation & verification

- [x] 5.1 Update `docs/command-protocols.md` to reference the canonical vocabulary as the source of truth (added Canonical Vocabulary section + aligned names)
- [x] 5.2 Update `README.md` command/MCP listings to canonical names (Store commands + MCP operations rewritten as verb×type×locus)
- [x] 5.3 Ran `openspec validate` (PASS); core/CLI/MCP/agent all compile. CLI tests 10/13 pass; 3 failures (`ListCommandTest`, `AddCollectionTest`, `RemoveCollectionTest`) are **pre-existing** in-flight stub breakage, unmodified by this change
- [x] 5.4 Confirmed: all command/tool names match core vocabulary. Note: picocli/Spring annotations require literal name strings at declaration sites (cannot reference enums) — strict literal-elimination would need `static final String` constants in core (follow-up)
