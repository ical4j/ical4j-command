package org.ical4j.command.calendar;

import net.fortuna.ical4j.model.Calendar;
import picocli.CommandLine;

@CommandLine.Command(name = "freebusy", description = "Request free/busy information for a calendar resource")
public class FreeBusy extends AbstractCalendarCommand<Calendar> {

    @Override
    public Integer call() throws Exception {
        return 1;
    }
}
