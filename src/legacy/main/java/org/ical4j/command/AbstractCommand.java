package org.ical4j.command;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Base class for all commands that will invoke the specified consumer upon execution completion.
 * @param <T> the command result type
 */
public abstract class AbstractCommand<T> implements Callable<Integer> {

    /**
     * Output handler of the command result as provided by subclass implementations.
     */
    private final Consumer<T> outputHandler;

    /**
     * Default constructor. Prints command result to stdout.
     */
    public AbstractCommand() {
        this.outputHandler = DefaultOutputHandlers.STDOUT_PRINTER();
    }

    /**
     *
     * @param outputHandler the consumer of the command result.
     */
    public AbstractCommand(Consumer<T> outputHandler) {
        this.outputHandler = outputHandler;
    }

    public final Consumer<T> getOutputHandler() {
        return outputHandler;
    }
}
