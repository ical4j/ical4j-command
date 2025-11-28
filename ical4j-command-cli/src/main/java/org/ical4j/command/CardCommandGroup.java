package org.ical4j.command;

import org.ical4j.command.vcard.Serializer;
import picocli.CommandLine;

@CommandLine.Command(name = "card", description = "vCard operations",
        subcommands = { Serializer.class })
public class CardCommandGroup {

}
