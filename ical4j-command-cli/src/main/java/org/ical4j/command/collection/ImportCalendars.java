package org.ical4j.command.collection;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.command.collection.AbstractCollectionCommand;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

@CommandLine.Command(name = "import", description = "Update an object collection")
public class ImportCalendars extends AbstractCollectionCommand<CalendarCollection, List<Uid>> {

    public ImportCalendars() {
    }

    public ImportCalendars(String collection, Consumer<List<Uid>> consumer) {
        super(collection, consumer);
    }

    public ImportCalendars(String collection, Consumer<List<Uid>> consumer, ObjectStore<CalendarCollection> store) {
        super(collection, consumer, store);
    }

    @Override
    public Integer call() {
        return 1;
    }
}
