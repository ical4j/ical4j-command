package org.ical4j.command.entity;

import picocli.CommandLine;

@CommandLine.Command(name = "card", description = "vCard operations",
        subcommands = { Serializer.class })
public class CardCommandGroup {

}
