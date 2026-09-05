# Legacy command tree (quarantined)

Nothing under `src/legacy` is compiled. These are the picocli command classes, their support
classes and Spock specs from before the `single-native-binary` change, kept in place so each
operation can be re-admitted into the operation registry one at a time under the admission
bar in `openspec/changes/single-native-binary/design.md` (D8): a spec against an injected
store and a place in the native smoke suite.

Package paths are unchanged, so re-admitting a class is a `git mv` back under `src/main`
followed by the rewrite onto the registry. `docs/dropped-commands.md` lists every command
here with its intended (verb, type, locus).
