package org.ical4j.command.collection;

import org.ical4j.command.AbstractCollectionCommand;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.util.List;

import static org.ical4j.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;
import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "list-uids", description = "List object UIDs within a collection")
public class ListObjectUids extends AbstractCollectionCommand<ObjectCollection<?>, List<String>> {

    public ListObjectUids() {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
    }

    public ListObjectUids(ObjectStore<ObjectCollection<?>> store) {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER(), store);
    }

    public ListObjectUids(String collectionName, ObjectStore<ObjectCollection<?>> store) {
        super(collectionName, STDOUT_LIST_PRINTER(), store);
    }

    @Override
    public Integer call() {
        try {
            getConsumer().accept(getCollection().listObjectUids());
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
