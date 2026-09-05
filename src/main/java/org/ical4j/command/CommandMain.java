package org.ical4j.command;

import picocli.CommandLine;

/**
 * Root of the {@code ict} command tree.
 * <p>
 * Subcommands are not declared here. They are derived from the operation registry (see the
 * {@code single-native-binary} change) and attached at startup; until the registry exists the
 * tree is empty apart from help and version. The previous hand-written command classes live
 * under {@code src/legacy} until each is re-admitted.
 */
@CommandLine.Command(name = "ict", description = "@|bold,fg(104) iCal4j Command-line Tool|@",
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        commandListHeading = "%n@|bold,fg(104) Commands:|@%n",
        footer = "@|yellow Copyright (c) Ben Fortuna |@", showAtFileInUsageHelp = true)
public class CommandMain extends GlobalOptions {

    public static void main(String[] args) {
        LogLevel.configure(args);
        int exitCode = new CommandLine(new CommandMain()).execute(args);
        System.exit(exitCode);
    }
}
