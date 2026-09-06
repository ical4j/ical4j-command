package org.ical4j.command.ops;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    /** A described item or locus: name/value attributes in insertion order. */
    record Descriptor(Map<String, String> attributes) implements Result {
        public Descriptor {
            attributes.forEach((k, v) -> {
                if (k == null || v == null) {
                    throw new NullPointerException("Descriptor attributes cannot be null: " + k);
                }
            });
            attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
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

    /** One validation finding. */
    record ValidationEntry(String severity, String context, String message) {
    }

    /** A validation outcome: no entries means valid. */
    record Validation(List<ValidationEntry> entries) implements Result {
        public Validation {
            entries = List.copyOf(entries);
        }

        public boolean valid() {
            return entries.isEmpty();
        }
    }
}
