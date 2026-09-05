package org.ical4j.command.channel;

import net.fortuna.ical4j.model.Calendar;
import picocli.CommandLine;

@CommandLine.Command(name = "send-calendar", description = "Send a calendar on a channel")
public class SendCalendarCommand extends AbstractEgressChannelCommand<Calendar, Void> {

    private Calendar calendar;

    public SendCalendarCommand withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    @Override
    public Integer call() {
        boolean success = getEndpoint().send(() -> calendar);
        return success ? 0 : 1;
    }
}
