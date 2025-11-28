package org.ical4j.command.workspace;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "update-collection", description = "Update an object collection")
public class UpdateCollection extends AbstractWorkspaceCommand<ObjectCollection<?>, Void> {

    public UpdateCollection() {
    }

    public UpdateCollection(Consumer<Void> consumer) {
        super(consumer);
    }

    public UpdateCollection(Consumer<Void> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer);
        setStore(store);
    }

    @Override
    public Integer call() {
        return 1;
    }
}
