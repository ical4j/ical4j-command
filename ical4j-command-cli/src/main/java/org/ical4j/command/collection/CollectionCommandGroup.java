package org.ical4j.command.collection;

import picocli.CommandLine;

@CommandLine.Command(name = "collections", description = "Manage calendar and card collections",
        subcommands = {GetCalendar.class, ListCalendars.class, CreateCalendar.class, UpdateCalendar.class,
                DeleteCalendar.class, GetCard.class, ListCards.class, CreateCard.class, UpdateCard.class,
                DeleteCard.class,})
public class CollectionCommandGroup {

}
