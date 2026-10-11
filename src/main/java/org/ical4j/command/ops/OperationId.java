package org.ical4j.command.ops;

import org.ical4j.command.vocab.EntityType;
import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.command.vocab.Subject;
import org.ical4j.command.vocab.Verb;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The (verb, subject, locus) identity of an operation.
 */
public record OperationId(Verb verb, Subject subject, Locus locus) {

    public OperationId {
        Objects.requireNonNull(verb, "verb");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(locus, "locus");
        if (subject instanceof Locus l && !l.isSubject()) {
            throw new IllegalArgumentException("Locus " + l + " cannot be the subject of an operation");
        }
    }

    public static OperationId of(Verb verb, Subject subject, Locus locus) {
        return new OperationId(verb, subject, locus);
    }

    /**
     * Parses the {@link #toString()} form: {@code "list event in collection"} or
     * {@code "generate event"} (locus {@link Locus#NONE}).
     *
     * @throws IllegalArgumentException if any word is not in the vocabulary
     */
    public static OperationId parse(String text) {
        String[] words = text.trim().split("\\s+");
        if (words.length != 2 && !(words.length == 4 && "in".equals(words[2]))) {
            throw new IllegalArgumentException("Expected '<verb> <subject> [in <locus>]': " + text);
        }
        Verb verb = enumFor(Verb.class, words[0]);
        Subject subject = subjectFor(words[1]);
        Locus locus = words.length == 4 ? enumFor(Locus.class, words[3]) : Locus.NONE;
        return new OperationId(verb, subject, locus);
    }

    /** Resolves a subject key across entity types, serialization families and container loci. */
    public static Subject subjectFor(String key) {
        return Stream.<Subject>concat(Stream.concat(Stream.of(EntityType.values()), Stream.of(SerializationFamily.values())),
                        Stream.of(Locus.values()).filter(Locus::isSubject))
                .filter(s -> s.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown subject: " + key));
    }

    private static <E extends Enum<E>> E enumFor(Class<E> type, String key) {
        try {
            return Enum.valueOf(type, key.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName().toLowerCase(Locale.ROOT) + ": " + key);
        }
    }

    public String verbKey() {
        return verb.name().toLowerCase(Locale.ROOT);
    }

    public String subjectKey() {
        return subject.key();
    }

    /** CLI-style rendering: {@code list event in collection}. */
    @Override
    public String toString() {
        return locus == Locus.NONE ? verbKey() + " " + subjectKey()
                : verbKey() + " " + subjectKey() + " in " + locus.key();
    }
}
