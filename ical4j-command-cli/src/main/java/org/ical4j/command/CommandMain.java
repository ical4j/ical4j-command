package org.ical4j.command;

import picocli.CommandLine;

@CommandLine.Command(name = "ical4j", description = "iCal4j Command Framework",
        subcommands = {CalendarCommandGroup.class, CardCommandGroup.class, CollectionCommandGroup.class,
                ChannelCommandGroup.class, WorkspaceCommandGroup.class, TemplateCommandGroup.class,
        ConfigureCommand.class},
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        footer = "Copyright (c) Ben Fortuna", showAtFileInUsageHelp = true)
public class CommandMain extends GlobalOptions {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandMain()).execute(args);
        System.exit(exitCode);
    }
}
