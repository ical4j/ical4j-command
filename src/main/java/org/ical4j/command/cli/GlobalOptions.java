package org.ical4j.command.cli;

import net.fortuna.ical4j.util.CompatibilityHints;
import org.ical4j.command.ops.Format;
import picocli.CommandLine;

/**
 * Options accepted by every command in the tree. They are inherited, so they may appear before
 * or after the verb and type: {@code ict --format json list collection} and
 * {@code ict list collection --format json} are the same.
 */
public class GlobalOptions {

    private String workspace;

    private Format format = Format.TEXT;

    private int verbosity;

    @CommandLine.Option(names = "--workspace", paramLabel = "<workspace>", scope = CommandLine.ScopeType.INHERIT,
            description = "Workspace to act in (default: the active workspace from configuration)")
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @CommandLine.Option(names = "--format", paramLabel = "<format>", scope = CommandLine.ScopeType.INHERIT,
            description = "Output format: text, json or xml (default: text)")
    public void setFormat(Format format) {
        this.format = format;
    }

    @CommandLine.Option(names = "--lenient", description = "Enable lenient parsing", scope = CommandLine.ScopeType.INHERIT)
    public void setRelaxedParsing(boolean relaxedParsing) {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, relaxedParsing);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, relaxedParsing);
    }

    /**
     * Declared so that the flag appears in usage help and is accepted by the parser. The level
     * itself is applied earlier, from the raw arguments, by {@link LogLevel#configure(String[])}.
     */
    @CommandLine.Option(names = {"-v", "--verbose"}, scope = CommandLine.ScopeType.INHERIT,
            description = "Increase logging verbosity (-v info, -vv debug, -vvv trace)")
    public void setVerbose(boolean[] verbose) {
        this.verbosity = verbose.length;
    }

    /** As {@link #setVerbose(boolean[])}: accepted here, applied by {@link LogLevel}. */
    @CommandLine.Option(names = {"-q", "--quiet"}, scope = CommandLine.ScopeType.INHERIT,
            description = "Log errors only")
    public void setQuiet(boolean quiet) {
        // handled by LogLevel before parsing
    }

    /** The {@code --workspace} value, or {@code null} to use the configured active workspace. */
    public String workspace() {
        return workspace;
    }

    public Format format() {
        return format;
    }

    public int verbosity() {
        return verbosity;
    }
}
