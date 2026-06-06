# iCal4j Command Line

A command framework for managing iCalendar and vCard data.

## Overview

iCal4j Command is a collection of operations for common iCalendar and vCard
use cases. This includes direct manipulation of data objects, integration with
persistence stores, and systems interoperability.

### Example Use Cases

| Use Case     | Description                                     |
|--------------|-------------------------------------------------|
| Workspaces   | Create workspaces for distinct knowledge graphs |
| Collections  | Group related activities, notes and contacts    |
| Events       | Schedule and manage events and tasks            |
| Actions      | Track actions and to-dos                        |
| Issues       | Manage issues and bug reports                   |
| Notes        | Maintain notes and annotations                  |
| Observances  | Reminders for important dates                   |
| Reports      | Report on past activities and events            |
| Availability | Share free/busy information                     |
| Entities     | Maintain contact and organization information   |

## Command Line Interface (CLI)

The CLI provides access to iCal4j Command operations via a terminal
interface. It supports a variety of commands for manipulating iCalendar and
vCard data, as well as managing workspaces and collections.

### Calendar Commands

The following commands directly manipulate iCalendar objects:

* Validator - check validity of iCalendar data
* Filter - select a subset of iCalendar components using filter expressions
* Free Busy - generate free/busy information for specified iCalendar data
* Replace UIDs - regenerate UIDs for all iCalendar components
* Serialize - output iCalendar data in various formats (e.g. XML, JSON)
* Deserialize - read iCalendar data from various formats (e.g. XML, JSON)

### vCard Commands

* ...


### Strategy Commands

Strategy commands use the `generate` verb (command group `generate`, alias `new`):

* Generate Event - generate/overlay iCalendar data using strategy patterns

### Store Commands

* Add Workspace - add a new workspace for storing iCalendar/vCard collections
* List Workspaces - list available workspaces
* Remove Workspace - remove an existing workspace
* Get Workspace - retrieve details of a specific workspace

### Collection Commands

* ...

### Channel Commands

* ...


## Model Context Protocol (MCP)

iCal4j Command supports the Model Context Protocol (MCP) for managing
iCalendar and vCard data in a structured manner. MCP defines a set of
operations for creating, retrieving, updating, and deleting data objects within
a defined context.

### MCP Operations

MCP operations follow the canonical vocabulary (see `docs/command-protocols.md`): a **verb**
applied to an **entity type** within a **locus**. The supported verbs are:

* `add` / `remove` — add or remove an item within a locus (e.g. add a workspace, remove a
  collection)
* `list` — enumerate items of a type within a locus (e.g. list events in a collection)
* `get` — retrieve a single item or its metadata
* `update` — modify an existing item
* `import` / `export` — move calendar/card serializations into or out of a collection

These apply across the loci `workspace` and `collection`, and the entity types `entity`
(contact), `event`, `action`, `issue`, `note`, `observance`, `report`, `availability` and
`resource`. (`create`/`delete` are standardised on `add`/`remove`.)

## Agentic User Agent

iCal4j Command includes an Agentic User Agent that provides intelligent
automation capabilities for managing iCalendar and vCard data. The User Agent
can perform tasks such as scheduling events, sending notifications, and
managing contacts based on predefined rules and user preferences.

### Prompts

Some example prompts for the User Agent:

* "Create a new event for a team meeting next Monday at 10 AM."
* "Add a contact for John Doe with email"
* "Generate a free/busy report for the next two weeks."
* "List all upcoming events in my calendar."
* "Update the location of the event 'Project Kickoff' to 'Conference Room B'."
* "Delete the contact for Jane Smith."
* "Send a notification to all attendees of the event 'Weekly Sync'."
* "Create a new workspace for project management."
* "List all workspaces available in my account."
* "Add a new collection for storing project-related notes."

### Interactions

The User Agent can interact with users through various channels, including
email, messaging apps, and voice assistants. It can also integrate with other
systems and services to enhance its capabilities.

When prompted the agent may ask clarifying questions to ensure it understands the user's intent before taking action.

For example:

**User:** "Create birthday reminders for my family members."
**Agent:** "Could you please provide the names and birth dates of your family members?"



## System Properties

The following table describes System properties applicable to the iCal4j User Agent.


| System Property                 | Description                                                                     | Value                                                | Default                                      |
|---------------------------------|---------------------------------------------------------------------------------|------------------------------------------------------|----------------------------------------------|
| org.ical4j.command.prodid       | An application identifier used as the value for the iCalendar `PRODID` property | A string (e.g. `-//Apple Inc.//Mac OS X 10.8.5//EN`) | `-//iCal4j//User Agent//EN`                  |
| org.ical4j.command.organizer    | The default URI used as the value for the iCalendar `ORGANIZER` property        | A URI (e.g. `mailto:johnd@example.com`)              | -                                            |
| org.ical4j.command.uidgenerator | The `UidGenerator` implementation used to create `UID` properties               | A fully qualified class name                         | `net.fortuna.ical4j.util.RandomUidGenerator` |
 
