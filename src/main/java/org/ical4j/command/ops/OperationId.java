package org.ical4j.command.ops;

import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.Subject;
import org.ical4j.command.vocab.Verb;

import java.util.Locale;
import java.util.Objects;

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
