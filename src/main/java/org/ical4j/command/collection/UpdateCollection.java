package org.ical4j.command.collection;

import org.ical4j.command.AbstractStoreCommand;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "update", description = "Update an object collection")
public class UpdateCollection extends AbstractStoreCommand<ObjectCollection<?>, Void> {

    public UpdateCollection() {
        super();
    }

    public UpdateCollection(Consumer<Void> consumer) {
        super(consumer);
    }

    public UpdateCollection(Consumer<Void> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer, store);
    }

    @Override
    public Integer call() {
        return 1;
    }
}
