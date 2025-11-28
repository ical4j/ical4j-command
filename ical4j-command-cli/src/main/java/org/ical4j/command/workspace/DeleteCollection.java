package org.ical4j.command.workspace;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

/**
 * A command to delete an existing collection from an object store.
 */
@CommandLine.Command(name = "delete-collection", description = "Purge a collection")
public class DeleteCollection extends AbstractWorkspaceCommand<ObjectCollection<?>, ObjectCollection<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCollection.class);

    @CommandLine.Option(names = {"-name"})
    private String collectionName;

    public DeleteCollection() {
    }

    public DeleteCollection(Consumer<ObjectCollection<?>> consumer) {
        super(consumer);
    }

    public DeleteCollection(Consumer<ObjectCollection<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer);
        setStore(store);
    }

    public DeleteCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public Integer call() {
        try {
            ObjectCollection<?> collection = getStore().getCollection(collectionName);
            collection.delete();
            getOutputHandler().accept(collection);
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
