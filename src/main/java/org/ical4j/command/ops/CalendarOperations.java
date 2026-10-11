package org.ical4j.command.ops;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentContainer;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Uid;
import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.command.vocab.Verb;
import org.ical4j.connector.CalendarCollection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                    // The connector's merge splits the calendar by UID and fails with a
                    // NullPointerException when a component references a TZID the calendar
                    // carries no VTIMEZONE for, so complete the time zones first.
                    Calendar calendar = withTimeZones(Inputs.calendar(context, params));
                    Uid[] uids = Stores.call(() -> collection.merge(calendar));
                    return new Result.Identifiers(Arrays.stream(uids).map(Uid::getValue).toList());
                })
                .build();
    }

    static Operation exportCalendar() {
        return Operation.define(Verb.EXPORT, SerializationFamily.CALENDAR, Locus.COLLECTION)
                .description("Export one stored calendar object by UID, or the whole collection as one calendar,"
                        + " with a VTIMEZONE for every TZID it references")
                .params(Stores.COLLECTION, UID)
                .handler((context, params) -> {
                    CalendarCollection collection = Stores.calendarCollection(context, params.require("collection"));
                    Calendar calendar = params.string("uid").isPresent()
                            ? Stores.call(() -> collection.getCalendar(params.require("uid")))
                            : Stores.call(collection::export);
                    if (calendar == null) {
                        throw new OperationException.NotFound("No such calendar: " + params.string("uid", ""));
                    }
                    return new Result.CalendarData(withTimeZones(calendar));
                })
                .build();
    }

    /**
     * Adds a VTIMEZONE from ical4j's registry for each TZID referenced without one, so the
     * exported stream stands alone. Unknown TZIDs are left as they are.
     */
    static Calendar withTimeZones(Calendar calendar) {
        Set<String> defined = new HashSet<>();
        Set<String> referenced = new LinkedHashSet<>();
        collectTzIds(calendar.getComponents(), defined, referenced);
        referenced.removeAll(defined);
        if (referenced.isEmpty()) {
            return calendar;
        }
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        Calendar result = calendar.copy();
        for (String tzid : referenced) {
            TimeZone tz = registry.getTimeZone(tzid);
            if (tz != null) {
                result.add(tz.getVTimeZone());
            }
        }
        return result;
    }

    private static void collectTzIds(List<? extends Component> components, Set<String> defined, Set<String> referenced) {
        for (Component c : components) {
            if (c instanceof VTimeZone) {
                c.getProperty(Property.TZID).ifPresent(p -> defined.add(p.getValue()));
                continue;
            }
            for (Property p : c.getProperties()) {
                p.getParameter(Parameter.TZID).ifPresent(tzid -> referenced.add(tzid.getValue()));
            }
            if (c instanceof ComponentContainer<?> container) {
                collectTzIds(container.getComponents(), defined, referenced);
            }
        }
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
