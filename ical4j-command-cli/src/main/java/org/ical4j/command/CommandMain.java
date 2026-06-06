package org.ical4j.command;

import org.ical4j.command.collection.AddCollection;
import org.ical4j.command.collection.RemoveCollection;
import org.ical4j.command.config.ConfigureCommand;
import org.ical4j.command.metadata.AddCommandGroup;
import org.ical4j.command.metadata.GetProperty;
import org.ical4j.command.metadata.UnsetProperty;
import org.ical4j.command.metadata.SetCommandGroup;
import org.ical4j.command.revisions.ListRevisions;
import org.ical4j.command.revisions.RevertRevision;
import org.ical4j.command.revisions.UndoRevisions;
import org.ical4j.command.strategy.StrategyCommandGroup;
import org.ical4j.command.strategy.StrategyInfo;
import org.ical4j.command.workspace.ActivateWorkspace;
import org.ical4j.command.workspace.ListWorkspaces;
import org.ical4j.command.workspace.PrintWorkspace;
import picocli.CommandLine;

@CommandLine.Command(name = "ict", description = "@|bold,fg(104) iCal4j Command-line Tool|@",
        subcommands = {
                // Collection commands
                AddCollection.class, RemoveCollection.class,
                // Common commands
                ListCommand.class, CopyCommand.class, MoveCommand.class, DeleteCommand.class, EditCommand.class, PrintCommand.class,
                // Strategy commands
                StrategyCommandGroup.class, StrategyInfo.class,
                // Metadata commands
                AddCommandGroup.class, GetProperty.class, SetCommandGroup.class, UnsetProperty.class,
                // Import/export commands
                ImportCalendars.class, ExportCommand.class,
                // Workspace commands
                ListWorkspaces.class, ActivateWorkspace.class, PrintWorkspace.class,
                // Revision commands
                ListRevisions.class, RevertRevision.class, UndoRevisions.class,
                // Utility commands
                ValidatorCommand.class, FilterCommand.class, ConverterCommand.class,
                ConfigureCommand.class},
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        commandListHeading = "%n@|bold,fg(104) Common commands:|@%n",
        footer = "@|yellow Copyright (c) Ben Fortuna |@", showAtFileInUsageHelp = true)
public class CommandMain extends GlobalOptions {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandMain()).execute(args);
        System.exit(exitCode);
    }
}
