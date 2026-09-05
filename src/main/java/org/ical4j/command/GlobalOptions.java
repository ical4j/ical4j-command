package org.ical4j.command;

import net.fortuna.ical4j.util.CompatibilityHints;
import picocli.CommandLine;

public class GlobalOptions {

    @CommandLine.Option(names = "-lenient", description = "Enable lenient parsing", scope = CommandLine.ScopeType.INHERIT)
    public void setRelaxedParsing(boolean relaxedParsing) {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    }

    /**
     * Declared so that the flag appears in usage help and is accepted by the parser. The level
     * itself is applied earlier, from the raw arguments, by {@link LogLevel#configure(String[])}.
     */
    @CommandLine.Option(names = {"-v", "--verbose"}, scope = CommandLine.ScopeType.INHERIT,
            description = "Increase logging verbosity (-v info, -vv debug, -vvv trace)")
    public void setVerbose(boolean[] verbose) {
        // handled by LogLevel before parsing
    }
}
