package org.ical4j.command.calendar;

import picocli.CommandLine;

@CommandLine.Command(name = "calendar", description = "Calendar operations",
        subcommands = {Serializer.class, FilterCalendar.class, Validator.class, ReplaceUids.class})
public class CalendarCommandGroup {

}
