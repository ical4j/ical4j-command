package org.ical4j.command.ops;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.command.vocab.EntityType;
import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.Verb;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Listing entities within a collection. Calendar-backed types map to their iCalendar
 * component; {@code entity} lists the cards in a card collection.
 */
final class ListOperations {

    /** Entity types listed so far and the component that carries them. */
    static final Map<EntityType, String> COMPONENTS = Map.of(
            EntityType.EVENT, Component.VEVENT,
            EntityType.ACTION, Component.VTODO,
            EntityType.NOTE, Component.VJOURNAL);

    private ListOperations() {
    }

    static List<Operation> all() {
        List<Operation> ops = new ArrayList<>();
        for (EntityType type : List.of(EntityType.EVENT, EntityType.ACTION, EntityType.NOTE)) {
            ops.add(listComponents(type));
        }
        ops.add(listEntities());
        return ops;
    }

    static Operation listComponents(EntityType type) {
        String component = COMPONENTS.get(type);
        return Operation.define(Verb.LIST, type, Locus.COLLECTION)
                .description("List the " + type.key() + "s in a collection")
                .params(Stores.COLLECTION)
                .handler((context, params) -> {
                    CalendarCollection collection = Stores.calendarCollection(context, params.require("collection"));
                    List<Calendar> calendars = Stores.call(collection::getAll);
                    List<Result.Descriptor> items = new ArrayList<>();
                    for (Calendar calendar : calendars) {
                        for (CalendarComponent c : calendar.<CalendarComponent>getComponents()) {
                            if (component.equals(c.getName())) {
                                items.add(describe(c));
                            }
                        }
                    }
                    return new Result.Descriptors(items);
                })
                .build();
    }

    static Operation listEntities() {
        return Operation.define(Verb.LIST, EntityType.ENTITY, Locus.COLLECTION)
                .description("List the entities (cards) in a collection")
                .params(Stores.COLLECTION)
                .handler((context, params) -> {
                    CardCollection collection = Stores.cardCollection(context, params.require("collection"));
                    List<VCard> cards = Stores.call(collection::getAll);
                    List<Result.Descriptor> items = new ArrayList<>();
                    for (VCard card : cards) {
                        Map<String, String> attributes = new LinkedHashMap<>();
                        attributes.put("uid", Stores.call(() -> card.getUid().getValue()));
                        attributes.put("name", card.getEntities().stream()
                                .flatMap(e -> e.<Property>getProperty("FN").stream())
                                .map(Property::getValue).findFirst().orElse(""));
                        items.add(new Result.Descriptor(attributes));
                    }
                    return new Result.Descriptors(items);
                })
                .build();
    }

    private static Result.Descriptor describe(CalendarComponent c) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("uid", c.getUid().map(Property::getValue).orElse(""));
        attributes.put("summary", value(c, "SUMMARY"));
        attributes.put("start", value(c, "DTSTART"));
        return new Result.Descriptor(attributes);
    }

    private static String value(CalendarComponent c, String property) {
        Optional<Property> p = c.getProperty(property);
        return p.map(Property::getValue).orElse("");
    }
}
