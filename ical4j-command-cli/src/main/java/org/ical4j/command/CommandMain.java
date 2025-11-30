package org.ical4j.command;

import org.ical4j.command.collection.CollectionCommandGroup;
import org.ical4j.command.strategy.StrategyCommandGroup;
import org.ical4j.command.workspace.WorkspaceCommandGroup;
import picocli.CommandLine;

@CommandLine.Command(name = "ical4j", description = "iCal4j Command-line Tool",
        subcommands = {ValidatorCommand.class, FilterCommand.class, ConverterCommand.class,
                StrategyCommandGroup.class, CollectionCommandGroup.class, WorkspaceCommandGroup.class,
        ConfigureCommand.class},
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        footer = "Copyright (c) Ben Fortuna", showAtFileInUsageHelp = true)
public class CommandMain extends GlobalOptions {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandMain()).execute(args);
        System.exit(exitCode);
    }
}
