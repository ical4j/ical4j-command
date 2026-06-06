package org.ical4j.command.card;

import picocli.CommandLine;

@CommandLine.Command(name = "card", description = "vCard operations",
        subcommands = { Serializer.class })
public class CardCommandGroup {

}
