package org.ical4j.command;

import org.ical4j.command.collection.*;
import picocli.CommandLine;

@CommandLine.Command(name = "collection", description = "Calendar and card collection operations",
        subcommands = {GetCalendar.class, ListCalendars.class, CreateCalendar.class, UpdateCalendar.class,
                DeleteCalendar.class, GetCard.class, ListCards.class, CreateCard.class, UpdateCard.class,
                DeleteCard.class,})
public class CollectionCommandGroup {

}
