package org.ical4j.command.strategy;

import picocli.CommandLine;

@CommandLine.Command(name = "generate", aliases = {"new"}, description = "Generate objects using predefined strategies",
        subcommands = {CreateAction.class, CreateEntity.class, CreateEvent.class,
         CreateIssue.class, CreateNote.class, CreateObservance.class})
public class StrategyCommandGroup {

}
