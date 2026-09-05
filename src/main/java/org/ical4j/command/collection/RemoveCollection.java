package org.ical4j.command.collection;

import org.ical4j.command.config.CommandConfig;
import org.ical4j.command.workspace.AbstractWorkspaceCommand;
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
@CommandLine.Command(name = "rmcol", description = "Purge a collection")
public class RemoveCollection extends AbstractWorkspaceCommand<ObjectCollection<?>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCollection.class);

    @CommandLine.Parameters(index = "0", description = "Name of the collection to remove")
    private String collectionName;

    @CommandLine.Option(names = "--type",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "CALENDAR")
    private AddCollection.CollectionType collectionType;

    public RemoveCollection() {
    }

    public RemoveCollection(Consumer<String> outputHandler, ObjectStore<ObjectCollection<?>> store) {
        super(outputHandler);
        setStore(store);
    }

    public RemoveCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public Integer call() {
        try {
            // Injected store: remove directly from the supplied store.
            if (getStore() != null) {
                ObjectCollection<?> collection = getStore().getCollection(collectionName);
                collection.delete();
                getOutputHandler().accept("Collection '" + collectionName + "' deleted");
                return 0;
            }

            // CLI path: resolve the appropriate store from configuration by type.
            ObjectCollection<?> collection;
            if (collectionType == AddCollection.CollectionType.CARD) {
                collection = CommandConfig.INSTANCE.getCardStore().getCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
            } else {
                collection = CommandConfig.INSTANCE.getCalendarStore().getCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
            }
            collection.delete();
            getOutputHandler().accept("Collection '" + collectionName + "' deleted");
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
