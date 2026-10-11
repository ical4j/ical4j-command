package org.ical4j.command.ops;

/**
 * The executable part of an operation. Implementations return a structured result, throw an
 * {@link OperationException}, and never write to stdout or stderr.
 */
@FunctionalInterface
public interface Handler {

    Result apply(Context context, Params params);
}
