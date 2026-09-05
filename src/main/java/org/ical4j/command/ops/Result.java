package org.ical4j.command.ops;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;

import java.util.List;
import java.util.Map;

/**
 * A structured operation result. Handlers return one of these; surfaces render them. No result
 * carries pre-formatted text for a particular surface.
 */
public interface Result {

    /** A short outcome statement, for operations that change state and return nothing else. */
    record Status(String message) implements Result {
    }

    /** Identifiers of items, for {@code list}-style operations. */
    record Identifiers(List<String> ids) implements Result {
        public Identifiers {
            ids = List.copyOf(ids);
        }
    }

    /** A described item or locus: ordered name/value attributes. */
    record Descriptor(Map<String, String> attributes) implements Result {
        public Descriptor {
            attributes = Map.copyOf(attributes);
        }
    }

    /** Several descriptors, for listing loci. */
    record Descriptors(List<Descriptor> items) implements Result {
        public Descriptors {
            items = List.copyOf(items);
        }
    }

    /** An iCalendar object. */
    record CalendarData(Calendar calendar) implements Result {
    }

    /** A vCard object. */
    record CardData(VCard card) implements Result {
    }

    /** A validation outcome: empty entries mean valid. */
    record Validation(List<String> entries) implements Result {
        public Validation {
            entries = List.copyOf(entries);
        }

        public boolean valid() {
            return entries.isEmpty();
        }
    }
}
