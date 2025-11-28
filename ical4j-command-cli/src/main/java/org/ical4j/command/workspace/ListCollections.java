package org.ical4j.command.workspace;

import org.ical4j.command.config.StoreConfiguration;
import org.ical4j.connector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

@CommandLine.Command(name = "list-collections", description = "List collections in an object store")
public class ListCollections extends AbstractWorkspaceCommand<ObjectCollection<?>, List<? extends ObjectCollection<?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCollections.class);

    public ListCollections() {
    }

    public ListCollections(Consumer<List<? extends ObjectCollection<?>>> consumer) {
        this(consumer, new StoreConfiguration().getCalendarStore());
    }

    public ListCollections(Consumer<List<? extends ObjectCollection<?>>> consumer, ObjectStore<? extends ObjectCollection<?>> store) {
        super(consumer);
//        setStore(store);
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getStore().getCollections(getWorkspace()));
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
