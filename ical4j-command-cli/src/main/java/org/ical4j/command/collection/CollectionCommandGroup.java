package org.ical4j.command.collection;

import org.ical4j.command.ImportCalendars;
import org.ical4j.command.ListCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "collections", description = "Manage calendar and card collections",
        subcommands = {AddCollection.class, ListCommand.class,
                RemoveCollection.class, ImportCalendars.class})
public class CollectionCommandGroup {

}
