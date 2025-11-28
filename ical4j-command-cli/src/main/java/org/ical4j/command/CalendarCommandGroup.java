package org.ical4j.command;

import org.ical4j.command.calendar.FilterCalendar;
import org.ical4j.command.calendar.ReplaceUids;
import org.ical4j.command.calendar.Serializer;
import org.ical4j.command.calendar.Validator;
import picocli.CommandLine;

@CommandLine.Command(name = "calendar", description = "Calendar operations",
        subcommands = {Serializer.class, FilterCalendar.class, Validator.class, ReplaceUids.class})
public class CalendarCommandGroup {

}
