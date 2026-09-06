package org.ical4j.command.ops;

import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;

import java.util.List;
import java.util.Locale;

/**
 * Store access shared by handlers: parameter conventions and the mapping of connector
 * exceptions onto {@link OperationException}.
 */
final class Stores {

    static final Param COLLECTION = Param.string("collection", "Name of the collection").asRequired();

    static final Param TYPE = Param.choice("type", "Collection type", SerializationFamily.class).defaultTo("calendar");

    private Stores() {
    }

    static SerializationFamily family(Params params) {
        return SerializationFamily.valueOf(params.string("type", "calendar").toUpperCase(Locale.ROOT));
    }

    static ObjectStore<? extends ObjectCollection<?>> store(Context context, SerializationFamily family) {
        return family == SerializationFamily.CARD ? context.cardStore() : context.calendarStore();
    }

    static CalendarCollection calendarCollection(Context context, String name) {
        return collection(context.calendarStore(), name, context.activeWorkspace());
    }

    static CardCollection cardCollection(Context context, String name) {
        return collection(context.cardStore(), name, context.activeWorkspace());
    }

    static <C extends ObjectCollection<?>> C collection(ObjectStore<C> store, String name, String workspace) {
        try {
            return store.getCollection(name, workspace);
        } catch (ObjectNotFoundException e) {
            throw new OperationException.NotFound("No such collection: " + name);
        } catch (ObjectStoreException e) {
            throw new OperationException.Store("Cannot open collection " + name + ": " + e.getMessage(), e);
        }
    }

    static boolean exists(ObjectStore<? extends ObjectCollection<?>> store, String name, String workspace) {
        try {
            store.getCollection(name, workspace);
            return true;
        } catch (ObjectNotFoundException e) {
            return false;
        } catch (ObjectStoreException e) {
            throw new OperationException.Store("Cannot open collection " + name + ": " + e.getMessage(), e);
        }
    }

    static <C extends ObjectCollection<?>> List<C> collections(ObjectStore<C> store, String workspace) {
        try {
            return store.getCollections(workspace);
        } catch (ObjectNotFoundException e) {
            // A workspace nobody has written to yet has no collections.
            return List.of();
        } catch (ObjectStoreException e) {
            throw new OperationException.Store("Cannot list collections: " + e.getMessage(), e);
        }
    }

    /** Runs a store action, translating connector failures. */
    static <T> T call(StoreCall<T> action) {
        try {
            return action.run();
        } catch (ObjectNotFoundException e) {
            throw new OperationException.NotFound(e.getMessage() == null ? "Not found" : e.getMessage());
        } catch (OperationException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationException.Store(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), e);
        }
    }

    @FunctionalInterface
    interface StoreCall<T> {
        T run() throws Exception;
    }
}
