# iCal4j Command Protocols

This document describes the various command protocols and tools provided by the iCal4j Command library for managing iCalendar and vCard data.

## Canonical Vocabulary

All command surfaces (CLI, MCP, agent) name operations from a single controlled vocabulary
defined in the `ical4j-command-core` module. Each operation is identified by a **verb**, an
**entity type**, and a **locus** (container). Surfaces share these names but dispatch
independently.

- **Verbs** (`org.ical4j.command.core.Verb`): `list` (many), `get` (one), `generate`
  (synthesise from a strategy/template), `add` (place into a locus), `update`, `remove`
  (take out of a locus), `copy`, `move`, `filter`, `validate`, `import`, `export`, `send`,
  `receive`. There is no `create`/`delete` — these are standardised on `add`/`remove`.
- **Entity types** (`EntityType`): `event`, `action`, `issue`, `note`, `observance`,
  `availability`, `report`, `entity` (contact), `resource`. All live directly within a
  collection.
- **Loci** (`Locus`): `workspace` ⊃ `collection` (two levels; entities live directly in a
  collection).
- **Serialization families** (`SerializationFamily`): `calendar` (iCalendar), `card`
  (vCard). These are NOT loci or entity types; data crosses a collection boundary via
  `import`/`export`.
- **iTIP methods** (`SendMethod`): `publish`, `request`, `reply` are specialised forms of
  `send`, not independent verbs.

## Calendar Commands

### Validator
- Purpose: Validates iCalendar data for conformance to specifications
- Location: `org.ical4j.command.calendar.Validator`
- Usage: Validates iCalendar objects for RFC compliance and structural integrity

### Filter
- Purpose: Filters iCalendar components based on expressions
- Location: `org.ical4j.command.calendar.FilterCalendar`
- Usage: Selects subsets of iCalendar components using filter criteria

### Free/Busy
- Purpose: Generates availability information
- Location: `org.ical4j.command.calendar.FreeBusy`
- Usage: Creates free/busy time reports from calendar data

### Replace UIDs
- Purpose: Regenerates unique identifiers
- Location: `org.ical4j.command.calendar.ReplaceUids`
- Usage: Updates UIDs across all calendar components

## Collection Management

### Calendar Collections
- Import Calendar (`import-calendar`): Imports calendar data into a collection
- Export Calendar (`export-calendar`): Exports calendar data from a collection
- Remove Calendar (`remove-calendar`): Removes calendar data from a collection
- List: Enumerates calendar entities in a collection

### Card Collections
- Import Card (`import-card`): Imports vCard data into a collection
- Export Card (`export-card`): Exports vCard data from a collection
- Remove Card (`remove-card`): Removes vCard data from a collection
- List: Enumerates vCard entities in a collection

## Channel Operations

### Calendar Channels
- Send Calendar: Transmits calendar data to specified endpoints. The iTIP methods
  `publish`, `request` and `reply` are specialised forms of `send`.
- Receive Calendar: Accepts incoming calendar data

### vCard Channels
- Send vCard: Transmits vCard data to specified endpoints
- Receive vCard: Accepts incoming vCard data

## Store Operations

### Collection Management
- Add Collection: Adds a collection to a workspace
- Remove Collection: Removes a collection from a workspace
- Get Collection: Retrieves collection metadata
- List Collections: Enumerates available collections
- Update Collection: Modifies collection properties

## Strategy (Generation) Operations

### Event Templates
- Purpose: Generates calendar data using simplified templates (the `generate` verb)
- Location: `org.ical4j.command.strategy.CreateEvent` (command name `generate event`)
- Usage: Synthesises calendar entries from template definitions

## Configuration

### System Properties
The following properties can be configured:

- `org.ical4j.command.prodid`: Customizes the PRODID property
- `org.ical4j.command.organizer`: Sets default organizer URI
- `org.ical4j.command.uidgenerator`: Specifies UID generation implementation

## Common Patterns

### Input/Output Handling
- Input handling through `InputHandler` interface
- Output processing via `DefaultOutputHandlers`
- Support for various data formats and sources

### Command Groups
- Calendar Command Group: Calendar-specific operations
- Card Command Group: vCard-specific operations
- Channel Command Group: Communication operations
- Collection Command Group: Data collection management
- Store Command Group: Storage operations
- Template Command Group: Template-based generation

Each command follows a consistent protocol pattern:
1. Input validation
2. Operation execution
3. Result handling
4. Error management
