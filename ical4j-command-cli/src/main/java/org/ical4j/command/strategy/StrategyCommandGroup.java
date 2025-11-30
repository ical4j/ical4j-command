package org.ical4j.command.strategy;

import picocli.CommandLine;

@CommandLine.Command(name = "strategy", description = "Create calendar and card objects using predefined strategies",
        subcommands = {CreateAction.class, CreateEntity.class, CreateEvent.class, })
public class StrategyCommandGroup {

}
