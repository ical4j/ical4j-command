package org.ical4j.command.collection;

import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "query-collection",
        description = "Query a calendar collection for calendar subsets matching an expression")
public class QueryCollection extends AbstractCollectionCommand<ObjectCollection<?>, List<?>> {

    public QueryCollection() {
        super();
    }

    public QueryCollection(Consumer<List<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(DEFAULT_COLLECTION, consumer, store);
    }

    public QueryCollection(String collectionName, Consumer<List<?>> consumer, ObjectStore<ObjectCollection<?>> store) {
        super(collectionName, consumer, store);
    }

    @Override
    public Integer call() {
        //TODO: add support for get all calendars in calendar collection..
        return 1;
    }
}
