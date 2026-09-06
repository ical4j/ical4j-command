package org.ical4j.command.ops;

/**
 * Output representations a surface can ask a {@link Renderer} for.
 */
public enum Format {
    /** Human-readable: iCalendar or vCard for data, aligned columns for lists, plain lines otherwise. */
    TEXT,
    /** jCal (RFC 7265) and jCard (RFC 7095) for data, plain JSON objects and arrays otherwise. */
    JSON,
    /** xCal (RFC 6321) and xCard (RFC 6351) for calendar and card data only. */
    XML
}
