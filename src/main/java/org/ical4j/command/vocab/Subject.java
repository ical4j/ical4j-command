package org.ical4j.command.vocab;

import java.util.Locale;

/**
 * The type axis of an operation: what a verb acts on.
 * <p>
 * Entity types are the usual subjects, but a serialization family is the subject of
 * {@code import}/{@code export}/{@code validate}, and a locus is the subject when the container
 * itself is listed, fetched, added or removed ({@code list collection}, {@code add collection}).
 * {@link Locus#NONE} is never a valid subject; the registry rejects it.
 */
public interface Subject {

    /**
     * @return the enum constant name
     */
    String name();

    /**
     * @return the lowercase name used in command syntax and tool names
     */
    default String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
