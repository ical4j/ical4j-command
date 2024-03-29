package org.ical4j.command.collection;

import org.ical4j.command.AbstractStoreCommand;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;

@CommandLine.Command(name = "list", description = "List collections in an object store")
public class ListCollections extends AbstractStoreCommand<ObjectCollection<?>, List<ObjectCollection<?>>> {

    public ListCollections() {
        super(STDOUT_LIST_PRINTER());
    }

    public ListCollections(Consumer<List<ObjectCollection<?>>> consumer) {
        super(consumer);
    }

    public ListCollections(Consumer<List<ObjectCollection<?>>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    @Override
    public Integer call() {
        try {
            getConsumer().accept(getStore().getCollections());
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
