package org.ical4j.command.collection;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "get-calendar", description = "Retrieve an existing calendar")
public class GetCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCalendar.class);

    @CommandLine.Option(names = {"-uid"})
    private String calendarUid;

    public GetCalendar() {
        super();
    }

    public GetCalendar(String collectionName, Consumer<Calendar> consumer) {
        super(collectionName, consumer);
    }

    public GetCalendar(String collectionName, Consumer<Calendar> consumer, ObjectStore<CalendarCollection> store) {
        super(collectionName, consumer, store);
    }

    public GetCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getCollection().getCalendar(calendarUid));
        } catch (ObjectNotFoundException | ObjectStoreException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
