package org.ical4j.command.collection;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "delete-calendar", description = "Delete an existing calendar")
public class DeleteCalendar extends AbstractCollectionCommand<CalendarCollection, Calendar> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCalendar.class);

    @CommandLine.Option(names = {"-uid"})
    private String calendarUid;

    public DeleteCalendar() {
        super();
    }

    public DeleteCalendar(String collectionName, Consumer<Calendar> consumer) {
        super(collectionName, consumer);
    }

    public DeleteCalendar(ObjectStore<CalendarCollection> store) {
        super(DEFAULT_COLLECTION, store);
    }

    public DeleteCalendar(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, store);
    }

    public DeleteCalendar withCalendarUid(String calendarUid) {
        this.calendarUid = calendarUid;
        return this;
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getCollection().removeCalendar(calendarUid));
        } catch (ObjectStoreException | FailedOperationException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
