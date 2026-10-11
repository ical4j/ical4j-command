package org.ical4j.command.cli;

import java.util.Locale;

/**
 * Chooses the slf4j-simple log level before any logger exists.
 * <p>
 * slf4j-simple reads its configuration once, when the first logger is created, and offers no
 * API to change levels afterwards. Command classes create loggers in static initialisers as
 * picocli instantiates them, which happens before picocli has parsed {@code --verbose}. So the
 * level is decided here from the raw arguments and the environment, and published as a system
 * property that overrides {@code simplelogger.properties}.
 * <p>
 * Precedence: {@code ICT_LOG_LEVEL} (trace, debug, info, warn, error, off) wins over the flags;
 * {@code -v} selects info, {@code -vv} debug, {@code -vvv} trace; {@code -q} selects error;
 * otherwise the default from {@code simplelogger.properties} applies.
 */
final class LogLevel {

    static final String ENV_VAR = "ICT_LOG_LEVEL";

    private static final String PROPERTY = "org.slf4j.simpleLogger.defaultLogLevel";

    private LogLevel() {
    }

    static void configure(String[] args) {
        configure(args, System.getenv(ENV_VAR));
    }

    static void configure(String[] args, String envLevel) {
        String level = envLevel != null && !envLevel.isBlank() ? envLevel.trim().toLowerCase(Locale.ROOT)
                : fromVerbosity(countVerbose(args));
        if (level == null && isQuiet(args)) {
            level = "error";
        }
        if (level != null) {
            System.setProperty(PROPERTY, level);
        }
    }

    static int countVerbose(String[] args) {
        int count = 0;
        for (String arg : args) {
            if ("--".equals(arg)) {
                break;
            }
            if ("--verbose".equals(arg)) {
                count++;
            } else if (arg.length() > 1 && arg.charAt(0) == '-' && arg.charAt(1) != '-'
                    && arg.chars().skip(1).allMatch(c -> c == 'v')) {
                count += arg.length() - 1;
            }
        }
        return count;
    }

    static boolean isQuiet(String[] args) {
        for (String arg : args) {
            if ("--".equals(arg)) {
                break;
            }
            if ("-q".equals(arg) || "--quiet".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    static String fromVerbosity(int count) {
        switch (count) {
            case 0: return null;
            case 1: return "info";
            case 2: return "debug";
            default: return "trace";
        }
    }
}
