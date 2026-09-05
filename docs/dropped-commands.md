# Dropped commands and their registry tuples

Every hand-written picocli command was quarantined under `src/legacy` by the
`single-native-binary` change. None is compiled. This table records where each one is meant
to land in the operation registry as a (verb, type, locus) tuple from the vocabulary in
`org.ical4j.command.vocab`, so re-admission is a lookup rather than a rediscovery.

Conventions:

- **Locus `none`** marks operations that act on a stream or file rather than a store
  (`generate`, `validate` on a file, `export` from a file). The registry needs a no-locus form
  for these; the `Locus` enum today has only `workspace` and `collection`.
- **`export`/`import` with `--format`** replace the old `serialize`, `convert` and `print`
  commands: those were all "write this object out in some representation".
- **Entities by UID** live directly in a collection. The old `remove-calendar` / `remove-card`
  removed one stored object by identifier, so they become `remove <type> --uid`, not a
  `calendar`-family operation.
- **Cohort 1** is the set covered by the 13 Spock specs that were green at quarantine time.
  Those specs moved with their classes and are the admission tickets (design D8).
- **Not an operation** marks picocli group nodes that only held subcommands; the verb-first
  tree replaces them.

| Legacy class | Old invocation | Tuple (verb, type, locus) | Cohort | Notes |
|---|---|---|---|---|
| `collection.AddCollection` | `mkcol <name> --type` | (`add`, `collection`, `workspace`) | 1 | spec: `AddCollectionTest` |
| `collection.RemoveCollection` | `rmcol <name>` | (`remove`, `collection`, `workspace`) | 1 | spec: `RemoveCollectionTest` |
| `collection.GetCollectionDetails` | `get-collection-details <name>` | (`get`, `collection`, `workspace`) | 1 | spec: `GetCollectionDetailsTest` |
| `ListCommand` | `list [spec]` | (`list`, `<type>`, `collection`) | 1 | spec: `ListCommandTest`. Type comes from the `spec` argument today; one registry entry per entity type |
| `collection.ListObjectUids` | `list-uids` | (`list`, `<type>`, `collection`) | later | subsumed by `list` with a `--uids-only` output choice |
| `collection.ListCards` | `list-cards` | (`list`, `entity`, `collection`) | later | subsumed by `list entity` |
| `collection.UpdateCollection` | `update-collection` | (`update`, `collection`, `workspace`) | later | |
| `collection.CollectionCommandGroup` | `collections ...` | not an operation | | group node |
| `calendar.UpdateCalendar` | `import-calendar` | (`import`, `calendar`, `collection`) | 1 | spec: `UpdateCalendarTest` |
| `calendar.GetCalendar` | `export-calendar` | (`export`, `calendar`, `collection`) | 1 | spec: `GetCalendarTest` |
| `calendar.DeleteCalendar` | `remove-calendar <uid>` | (`remove`, `<type>`, `collection`) | 1 | spec: `DeleteCalendarTest`. Removes one stored object by UID |
| `card.UpdateCard` | `import-card` | (`import`, `card`, `collection`) | 1 | spec: `UpdateCardTest` |
| `card.GetCard` | `export-card` | (`export`, `card`, `collection`) | 1 | spec: `GetCardTest` |
| `card.DeleteCard` | `remove-card <uid>` | (`remove`, `entity`, `collection`) | 1 | spec: `DeleteCardTest` |
| `ImportCalendars` | `import` | (`import`, `calendar`, `collection`) | later | duplicate of `import-calendar` with a different input path; merge |
| `ImportCards` | `import-cards` | (`import`, `card`, `collection`) | later | duplicate of `import-card`; merge |
| `ExportCommand` | `export` | (`export`, `calendar`\|`card`, `collection`) | later | family chosen by collection type |
| `strategy.CreateCalendar` | `generate calendar` | (`generate`, `calendar`, `none`) | 1 | spec: `CreateCalendarTest` |
| `strategy.CreateCard` | `generate card` | (`generate`, `card`, `none`) | 1 | spec: `CreateCardTest` |
| `strategy.CreateEvent` | `generate event` | (`generate`, `event`, `none`) | later | option declarations broken (flags without values); needs a rewrite before a spec |
| `strategy.CreateAction` | `generate action` | (`generate`, `action`, `none`) | later | |
| `strategy.CreateIssue` | `generate issue` | (`generate`, `issue`, `none`) | later | |
| `strategy.CreateNote` | `generate note` | (`generate`, `note`, `none`) | later | |
| `strategy.CreateObservance` | `generate observance` | (`generate`, `observance`, `none`) | later | |
| `strategy.CreateEntity` | `generate entity` | (`generate`, `entity`, `none`) | later | |
| `strategy.StrategyInfo` | `strategy` | not an operation | | printed nothing; strategy listing belongs in `generate --help` |
| `strategy.StrategyCommandGroup` | `generate` / `new` | not an operation | | group node; `generate` is the verb |
| `ValidatorCommand` | `validate -file\|-url\|-collection\|-` | (`validate`, `calendar`\|`card`, `none`) and (`validate`, `<type>`, `collection`) | later | file form and collection form are two registry entries |
| `calendar.Validator` | `calendar validator` | (`validate`, `calendar`, `none`) | later | duplicate of `validate`; drop |
| `FilterCommand` | `filter --filter` | (`filter`, `<type>`, `collection`) and file form | later | expression grammar rejected `summary contains 'x'` during the spike; check before admitting |
| `calendar.FilterCalendar` | `calendar filter` | (`filter`, `<type>`, `collection`) | later | duplicate of `filter`; drop |
| `ConverterCommand` | `convert --format` | (`export`, `calendar`\|`card`, `none`) with `--format` | later | JCARD output cycles in Jackson on the JVM; XML/JSON calendar output works natively |
| `calendar.Serializer` | `calendar serialize` | (`export`, `calendar`, `none`) with `--format` | later | duplicate of `convert`; drop |
| `card.Serializer` | `card serialize` | (`export`, `card`, `none`) with `--format` | later | duplicate of `convert`; drop |
| `PrintCommand` | `print` | (`export`, `calendar`\|`card`, `collection`) text format | later | `export` with the default text renderer |
| `calendar.FreeBusy` | `freebusy` | (`generate`, `availability`, `none`) | later | free/busy is an `availability` entity generated from input |
| `calendar.ReplaceUids` | `replace-uids` | (`update`, `<type>`, `none`) | later | a transform over a stream; may become an `export --regenerate-uids` option instead |
| `CopyCommand` | `copy` / `cp` | (`copy`, `<type>`, `collection`) | later | |
| `MoveCommand` | `move` / `mv` | (`move`, `<type>`, `collection`) | later | |
| `DeleteCommand` | `remove` / `rm` | (`remove`, `<type>`, `collection`) | later | generic form of `remove-calendar` / `remove-card` |
| `EditCommand` | `edit` | (`update`, `<type>`, `collection`) | later | |
| `metadata.GetProperty` | `get <property>` | (`get`, `<type>`, `collection`) with `--property` | later | property access is an option on `get`, not its own verb |
| `metadata.SetCommandGroup` | `set <property>` | (`update`, `<type>`, `collection`) with `--set` | later | |
| `metadata.UnsetProperty` | `unset <property>` | (`update`, `<type>`, `collection`) with `--unset` | later | |
| `metadata.AddAttachment` | `add attach` | (`update`, `<type>`, `collection`) with `--attach` | later | |
| `metadata.AddCategories` | `add categories` | (`update`, `<type>`, `collection`) with `--category` | later | |
| `metadata.AddComment` | `add comment` | (`update`, `<type>`, `collection`) with `--comment` | later | |
| `metadata.AddRelated` | `add related` | (`update`, `<type>`, `collection`) with `--related` | later | |
| `metadata.AddCommandGroup` | `add ...` | not an operation | | group node; `add` the verb means placing an item in a locus, not adding metadata |
| `metadata.PropertyCommandGroup` | (base class) | not an operation | | |
| `revisions.ListRevisions` | `revisions` | (`list`, `report`, `collection`) | later | revisions are a report over a collection; store support unverified |
| `revisions.RevertRevision` | `revert` | (`update`, `collection`, `workspace`) | later | |
| `revisions.UndoRevisions` | `undo` | (`update`, `collection`, `workspace`) | later | |
| `channel.SendCalendarCommand` | `send-calendar` | (`send`, `calendar`, `channel`) with `--method` | later | iTIP method via `SendMethod`; channel is not a `Locus` today |
| `channel.SendVCardCommand` | `send-card` | (`send`, `card`, `channel`) | later | |
| `channel.ReceiveCalendarCommand` | `receive-calendar` | (`receive`, `calendar`, `channel`) | later | |
| `channel.ReceiveVCardCommand` | `receive-card` | (`receive`, `card`, `channel`) | later | |
| `channel.ChannelCommandGroup` | `channel ...` | not an operation | | group node |
| `workspace.ListWorkspaces` | `list-workspaces` / `lws` | (`list`, `workspace`, `none`) | later | worked on the JVM and natively during the spike; an easy early admission |
| `workspace.ActivateWorkspace` | `activate` / `chws` | (`update`, `workspace`, `none`) with `--active` | later | could also be `ict config workspace.active` |
| `workspace.PrintWorkspace` | `print-workspace` / `pws` | (`get`, `workspace`, `none`) | later | failed on the JVM with "Unable to initialise collection config" |
| `workspace.WorkspaceCommandGroup` | `workspace ...` | not an operation | | group node |
| `config.ConfigureCommand` | `configure` | not an operation | | becomes `ict config` (task 3.4), outside the registry |
| `calendar.CalendarCommandGroup` | `calendar ...` | not an operation | | group node |
| `card.CardCommandGroup` | `card ...` | not an operation | | group node |

Support classes quarantined alongside, with no tuple of their own: `AbstractCommand`,
`DefaultOutputHandlers` (ported as renderers in task 2.6), `InputHandler`, `InputOptions`,
`OutputOptions`, `calendar.AbstractCalendarCommand`, `card.AbstractEntityCommand`,
`channel.AbstractEgressChannelCommand`, `channel.AbstractIngressChannelCommand`,
`collection.AbstractCollectionCommand`, `workspace.AbstractWorkspaceCommand`.

Vocabulary gaps this table exposes, to settle when the registry is defined (task 2.1):

- a no-locus form (`none`) for stream and file operations and for `generate`;
- `channel` as the locus of `send` / `receive`, which the vocabulary spec names but the
  `Locus` enum does not carry;
- `workspace` and `collection` as entity types when they are the thing being listed or
  fetched, alongside their role as loci.
