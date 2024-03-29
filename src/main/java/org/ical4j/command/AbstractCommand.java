package org.ical4j.command;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Base class for all commands that will invoke the specified consumer upon execution completion.
 * @param <T> the command result type
 */
public abstract class AbstractCommand<T> implements Callable<Integer> {

    private Consumer<T> consumer;

    public AbstractCommand() {
        this.consumer = DefaultOutputHandlers.STDOUT_PRINTER();
    }

    public AbstractCommand(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public AbstractCommand<T> withConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    public final Consumer<T> getConsumer() {
        return consumer;
    }
}
