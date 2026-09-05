package org.ical4j.command;

import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;

import static org.apache.logging.log4j.Level.*;

public class GlobalOptions {

    @CommandLine.Option(names = "-lenient", description = "Enable lenient parsing", scope = CommandLine.ScopeType.INHERIT)
    public void setRelaxedParsing(boolean relaxedParsing) {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    }

    @CommandLine.Option(names = {"-v", "--verbose"}, scope = CommandLine.ScopeType.INHERIT,
            description = "Increase logging verbosity")
    public void setVerbose(boolean[] verbose) {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(),
                verbose.length > 2 ? TRACE : verbose.length > 1 ? DEBUG : INFO);
    }
}
