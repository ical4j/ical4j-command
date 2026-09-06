package org.ical4j.command.ops;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.command.vocab.Verb;
import org.ical4j.connector.CalendarCollection;

import java.util.Arrays;
import java.util.List;

/**
 * Moving iCalendar data into and out of a collection.
 */
final class CalendarOperations {

    private static final Param UID = Param.string("uid", "UID of one stored calendar object");

    private CalendarOperations() {
    }

    static List<Operation> all() {
        return List.of(importCalendar(), exportCalendar(), removeCalendar());
    }

    static Operation importCalendar() {
        return Operation.define(Verb.IMPORT, SerializationFamily.CALENDAR, Locus.COLLECTION)
                .description("Import iCalendar data into a collection, merging by UID")
                .params(Stores.COLLECTION, Inputs.FILE)
                .handler((context, params) -> {
                    CalendarCollection collection = Stores.calendarCollection(context, params.require("collection"));
                    Calendar calendar = Inputs.calendar(context, params);
                    Uid[] uids = Stores.call(() -> collection.merge(calendar));
                    return new Result.Identifiers(Arrays.stream(uids).map(Uid::getValue).toList());
                })
                .build();
    }

    static Operation exportCalendar() {
        return Operation.define(Verb.EXPORT, SerializationFamily.CALENDAR, Locus.COLLECTION)
                .description("Export one stored calendar object by UID, or the whole collection as one calendar")
                .params(Stores.COLLECTION, UID)
                .handler((context, params) -> {
                    CalendarCollection collection = Stores.calendarCollection(context, params.require("collection"));
                    Calendar calendar = params.string("uid").isPresent()
                            ? Stores.call(() -> collection.getCalendar(params.require("uid")))
                            : Stores.call(collection::export);
                    if (calendar == null) {
                        throw new OperationException.NotFound("No such calendar: " + params.string("uid", ""));
                    }
                    return new Result.CalendarData(calendar);
                })
                .build();
    }

    static Operation removeCalendar() {
        return Operation.define(Verb.REMOVE, SerializationFamily.CALENDAR, Locus.COLLECTION)
                .description("Remove one stored calendar object by UID")
                .params(Stores.COLLECTION, UID.asRequired())
                .handler((context, params) -> {
                    CalendarCollection collection = Stores.calendarCollection(context, params.require("collection"));
                    String uid = params.require("uid");
                    // The connector's removeCalendar(uid) indexes into an empty list when the UID is
                    // unknown, so remove through removeAll and inspect the result.
                    List<Calendar> removed = Stores.call(() -> collection.removeAll(uid));
                    if (removed.isEmpty()) {
                        throw new OperationException.NotFound("No such calendar: " + uid);
                    }
                    return new Result.Status("Removed calendar '" + uid + "'");
                })
                .build();
    }
}
