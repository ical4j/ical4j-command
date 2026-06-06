package org.ical4j.command.collection;

import org.ical4j.command.AbstractCommand;
import org.ical4j.command.config.CommandConfig;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * A command to delete an existing collection from an object store.
 */
@CommandLine.Command(name = "rmcol", description = "Purge a collection")
public class RemoveCollection extends AbstractCommand<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCollection.class);

    @CommandLine.Parameters(index = "0", description = "Name of the collection to create")
    private String collectionName;

    @CommandLine.Option(names = "--type",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "CALENDAR")
    private AddCollection.CollectionType collectionType;

    public RemoveCollection() {
    }

    @Override
    public Integer call() {
        if (collectionType == AddCollection.CollectionType.CALENDAR) {
            try {
                ObjectCollection<?> collection = CommandConfig.INSTANCE.getCalendarStore().getCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
                collection.delete();
                getOutputHandler().accept("Collection '" + collectionName + "' deleted");
            } catch (ObjectStoreException | ObjectNotFoundException e) {
                LOGGER.error("Unexpected error", e);
                return 1;
            }
        }
        else if (collectionType == AddCollection.CollectionType.CARD) {
            try {
                ObjectCollection<?> collection = CommandConfig.INSTANCE.getCardStore().getCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
                collection.delete();
                getOutputHandler().accept("Collection '" + collectionName + "' deleted");
            } catch (ObjectStoreException | ObjectNotFoundException e) {
                LOGGER.error("Unexpected error", e);
                return 1;
            }
        }
        return 0;
    }
}
