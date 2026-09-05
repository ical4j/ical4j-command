package org.ical4j.command.channel;

import org.ical4j.command.AbstractCommand;
import org.ical4j.integration.IngressChannel;

import java.util.function.Consumer;

/**
 * Subclasses provide functionality that requires data store connectivity.
 *
 * @param <T> the supported collection type for a configured data store
 * @param <R> the command result consumer
 */
public abstract class AbstractIngressChannelCommand<T, R> extends AbstractCommand<R> {

    private final IngressChannel<T> endpoint;

    public AbstractIngressChannelCommand() {
        this.endpoint = null;
    }

    public AbstractIngressChannelCommand(IngressChannel<T> endpoint) {
        this.endpoint = endpoint;
    }

    public AbstractIngressChannelCommand(Consumer<R> consumer) {
        super(consumer);
        this.endpoint = null;
    }

    public AbstractIngressChannelCommand(Consumer<R> consumer, IngressChannel<T> endpoint) {
        super(consumer);
        this.endpoint = endpoint;
    }

    public IngressChannel<T> getEndpoint() {
        return endpoint;
    }
}
