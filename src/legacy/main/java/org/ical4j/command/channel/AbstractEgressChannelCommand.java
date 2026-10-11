package org.ical4j.command.channel;

import org.ical4j.command.AbstractCommand;
import org.ical4j.integration.EgressChannel;

import java.util.function.Consumer;

/**
 * Subclasses provide functionality that requires data store connectivity.
 *
 * @param <T> the supported collection type for a configured data store
 * @param <R> the command result consumer
 */
public abstract class AbstractEgressChannelCommand<T, R> extends AbstractCommand<R> {

    private final EgressChannel<T, R> endpoint;

    public AbstractEgressChannelCommand() {
        this.endpoint = null;
    }

    public AbstractEgressChannelCommand(EgressChannel<T, R> endpoint) {
        this.endpoint = endpoint;
    }

    public AbstractEgressChannelCommand(Consumer<R> consumer) {
        super(consumer);
        this.endpoint = null;
    }

    public AbstractEgressChannelCommand(Consumer<R> consumer, EgressChannel<T, R> endpoint) {
        super(consumer);
        this.endpoint = endpoint;
    }

    public EgressChannel<T, R> getEndpoint() {
        return endpoint;
    }
}
