package org.ical4j.command.collection;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;
import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "list-calendars", description = "List all existing calendars")
public class ListCalendars extends AbstractCollectionCommand<CalendarCollection, List<Calendar>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCalendars.class);

    public ListCalendars() {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
    }

    public ListCalendars(String collectionName, Consumer<List<Calendar>> consumer) {
        super(collectionName, consumer);
    }

    public ListCalendars(ObjectStore<CalendarCollection> store) {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER(), store);
    }

    public ListCalendars(String collectionName, ObjectStore<CalendarCollection> store) {
        super(collectionName, STDOUT_LIST_PRINTER(), store);
    }

    @Override
    public Integer call() {
        try {
            List<Calendar> calendars = new ArrayList<>();
            for (String uid : getCollection().listObjectUIDs()) {
                calendars.add(getCollection().getCalendar(uid));
            }
            getOutputHandler().accept(calendars);
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
