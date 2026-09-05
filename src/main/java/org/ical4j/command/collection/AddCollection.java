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
 * A command to create a new collection for a specified object store.
 */
@CommandLine.Command(name = "mkcol", description = "Add a new collection")
public class AddCollection extends AbstractWorkspaceCommand<ObjectCollection<?>, String> {

    public enum CollectionType {
        CALENDAR,
        CARD
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCollection.class);

    @CommandLine.Parameters(index = "0", description = "Name of the collection to create")
    private String collectionName;

    @CommandLine.Option(names = "--type",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "CALENDAR")
    private CollectionType collectionType;

    public AddCollection() {
    }

    public AddCollection(Consumer<String> outputHandler, ObjectStore<ObjectCollection<?>> store) {
        super(outputHandler);
        setStore(store);
    }

    public AddCollection withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public Integer call() {
        try {
            // Injected store: add directly to the supplied store.
            if (getStore() != null) {
                getStore().addCollection(collectionName);
                getOutputHandler().accept("Collection '" + collectionName + "' created successfully");
                return 0;
            }

            // CLI path: resolve the appropriate store from configuration by type.
            ObjectCollection<?> collection = null;
            if (collectionType == CollectionType.CALENDAR) {
                try {
                    CommandConfig.INSTANCE.getCalendarStore().getCollection(collectionName,
                            CommandConfig.INSTANCE.getActiveWorkspace());
                    getOutputHandler().accept("Collection with name '" + collectionName + "' already exists");
                    return 1;
                } catch (ObjectNotFoundException e) {
                }
                collection = CommandConfig.INSTANCE.getCalendarStore().addCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
            } else if (collectionType == CollectionType.CARD) {
                try {
                    CommandConfig.INSTANCE.getCardStore().getCollection(collectionName,
                            CommandConfig.INSTANCE.getActiveWorkspace());
                    getOutputHandler().accept("Collection with name '" + collectionName + "' already exists");
                    return 1;
                } catch (ObjectNotFoundException e) {
                }
                collection = CommandConfig.INSTANCE.getCardStore().addCollection(collectionName,
                        CommandConfig.INSTANCE.getActiveWorkspace());
            }
            if (collection != null) {
                getOutputHandler().accept("Collection '" + collectionName + "' created successfully");
            }
        } catch (ObjectStoreException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
