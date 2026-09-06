package org.ical4j.command.ops;

import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.command.vocab.Verb;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.local.LocalCalendarCollection;
import org.ical4j.connector.local.LocalCardCollection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations on collections within the active workspace.
 */
final class CollectionOperations {

    private static final Param NAME = Param.string("name", "Name of the collection").asRequired();

    private static final Param DESCRIPTION = Param.string("description", "Description of the collection");

    private CollectionOperations() {
    }

    static List<Operation> all() {
        return List.of(add(), get(), remove(), list());
    }

    static Operation add() {
        return Operation.define(Verb.ADD, Locus.COLLECTION, Locus.WORKSPACE)
                .description("Add a new collection to the active workspace")
                .params(NAME, Stores.TYPE, DESCRIPTION)
                .handler((context, params) -> {
                    String name = params.require("name");
                    SerializationFamily family = Stores.family(params);
                    ObjectStore<? extends ObjectCollection<?>> store = Stores.store(context, family);
                    if (Stores.exists(store, name, context.activeWorkspace())) {
                        throw new OperationException.AlreadyExists("Collection already exists: " + name);
                    }
                    ObjectCollection<?> created = Stores.call(() -> store.addCollection(name, context.activeWorkspace()));
                    // The two-argument addCollection leaves the display name unset; local
                    // collections expose it as a setter, so name them after their directory.
                    String description = params.string("description", null);
                    Stores.call(() -> {
                        if (created instanceof LocalCalendarCollection local) {
                            local.setDisplayName(name);
                            if (description != null) {
                                local.setDescription(description);
                            }
                        } else if (created instanceof LocalCardCollection local) {
                            local.setDisplayName(name);
                            if (description != null) {
                                local.setDescription(description);
                            }
                        }
                        return null;
                    });
                    return new Result.Status("Added " + family.key() + " collection '" + name + "'");
                })
                .build();
    }

    static Operation get() {
        return Operation.define(Verb.GET, Locus.COLLECTION, Locus.WORKSPACE)
                .description("Describe a collection in the active workspace")
                .params(NAME, Stores.TYPE)
                .handler((context, params) -> {
                    String name = params.require("name");
                    SerializationFamily family = Stores.family(params);
                    ObjectCollection<?> collection = Stores.collection(Stores.store(context, family), name,
                            context.activeWorkspace());
                    return describe(name, family, collection);
                })
                .build();
    }

    static Operation remove() {
        return Operation.define(Verb.REMOVE, Locus.COLLECTION, Locus.WORKSPACE)
                .description("Remove a collection and everything in it from the active workspace")
                .params(NAME, Stores.TYPE)
                .handler((context, params) -> {
                    String name = params.require("name");
                    SerializationFamily family = Stores.family(params);
                    ObjectCollection<?> collection = Stores.collection(Stores.store(context, family), name,
                            context.activeWorkspace());
                    Stores.call(() -> {
                        collection.delete();
                        return null;
                    });
                    return new Result.Status("Removed " + family.key() + " collection '" + name + "'");
                })
                .build();
    }

    static Operation list() {
        return Operation.define(Verb.LIST, Locus.COLLECTION, Locus.WORKSPACE)
                .description("List the collections in the active workspace")
                .handler((context, params) -> {
                    List<Result.Descriptor> items = new ArrayList<>();
                    for (ObjectCollection<?> c : Stores.collections(context.calendarStore(), context.activeWorkspace())) {
                        items.add(describe(c.getDisplayName(), SerializationFamily.CALENDAR, c));
                    }
                    for (ObjectCollection<?> c : Stores.collections(context.cardStore(), context.activeWorkspace())) {
                        items.add(describe(c.getDisplayName(), SerializationFamily.CARD, c));
                    }
                    return new Result.Descriptors(items);
                })
                .build();
    }

    private static Result.Descriptor describe(String name, SerializationFamily family, ObjectCollection<?> collection) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("name", name == null ? "" : name);
        attributes.put("type", family.key());
        attributes.put("description", collection.getDescription() == null ? "" : collection.getDescription());
        attributes.put("objects", String.valueOf(Stores.call(() -> collection.listObjectUIDs().size())));
        return new Result.Descriptor(attributes);
    }
}
