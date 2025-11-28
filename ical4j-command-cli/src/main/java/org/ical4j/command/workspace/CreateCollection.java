package org.ical4j.command.workspace;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

/**
 * A command to create a new collection for a specified object store.
 */
@CommandLine.Command(name = "create-collection", description = "Create a new collection")
public class CreateCollection extends AbstractWorkspaceCommand<ObjectCollection<?>, ObjectCollection<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCollection.class);

    @CommandLine.Option(names = {"-name"})
    private String collectionName;

    private String[] supportedComponents;

    public CreateCollection() {
    }

    public CreateCollection(Consumer<ObjectCollection<?>> consumer) {
        super(consumer);
    }

    public CreateCollection(Consumer<ObjectCollection<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(consumer);
        setStore(store);
    }

    public CreateCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public CreateCollection withSupportedComponents(String... supportedComponents) {
        this.supportedComponents = supportedComponents;
        return this;
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getStore().addCollection(collectionName));
        } catch (ObjectStoreException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
