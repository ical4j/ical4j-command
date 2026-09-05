package org.ical4j.command.channel;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.integration.event.CalendarPublishListener;
import org.ical4j.integration.flow.CalendarSubscriber;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "receive-calendar", description = "Receive a calendar from a channel")
public class ReceiveCalendarCommand extends AbstractIngressChannelCommand<Calendar, Calendar> {

    private long timeout;

    private boolean autoExpunge;

    public ReceiveCalendarCommand(Consumer<Calendar> consumer) {
        super(consumer);
    }

    @Override
    public Integer call() {
        CalendarSubscriber subscriber = new CalendarSubscriber();
        subscriber.addCalendarPublishListener(new CalendarPublishListener() {
            @Override
            public void onPublish(Calendar calendar) {
                getOutputHandler().accept(calendar);
            }
        });
        getEndpoint().subscribe(subscriber);
        boolean success = getEndpoint().poll(timeout, autoExpunge);
        return success ? 0 : 1;
    }
}
