## MODIFIED Requirements

### Requirement: The vocabulary lives in a shared core module
The canonical verbs, entity types, loci, serialization families and send methods SHALL be defined as enums in the `vocab` package of the single `ict` module. The operation registry and every surface SHALL reference these enums rather than declaring their own string literals. The `vocab` package SHALL contain vocabulary definitions only and MUST NOT contain dispatch or execution logic; dispatch lives in the operation registry.

#### Scenario: Surfaces reference vocabulary constants
- **WHEN** a surface names an operation
- **THEN** it derives the verb, type and locus names from the `vocab` enums via the operation's registry entry rather than declaring its own string literals

#### Scenario: Vocabulary package carries no dispatch
- **WHEN** the `vocab` package is inspected
- **THEN** it contains only vocabulary definitions (verbs, types, loci, families, send methods) and no command-execution or dispatch code

## REMOVED Requirements

### Requirement: Surfaces share vocabulary but dispatch independently
**Reason**: This requirement existed because the CLI (picocli) and MCP (Spring AI) surfaces were separate deployables with incompatible dispatch models. In a single binary, separate dispatch means every (verb, type, locus) operation is implemented twice with drift risk. Spring AI is removed by this change, so the incompatibility no longer exists.

**Migration**: Surfaces become views over the operation registry defined in the `operation-registry` capability. The scenario "No cross-surface execution coupling" is superseded by "Surfaces do not call each other" in that spec, which still forbids the MCP view calling CLI classes, but requires both to call the registry.
