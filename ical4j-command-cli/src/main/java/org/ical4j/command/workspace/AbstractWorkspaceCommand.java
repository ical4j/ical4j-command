package org.ical4j.command.workspace;

import org.ical4j.command.AbstractCommand;
import org.ical4j.connector.ObjectCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.function.Consumer;

/**
 * Subclasses provide functionality that requires workspace context.
 *
 * @param <R> the command result consumer
 */
public abstract class AbstractWorkspaceCommand<T extends ObjectCollection<?>, R> extends AbstractCommand<R> {

    private ObjectStore<T> store;

    @CommandLine.Option(names = {"-workspace"})
    private String workspace;

    public AbstractWorkspaceCommand() {
    }

    public AbstractWorkspaceCommand(Consumer<R> consumer) {
        super(consumer);
    }

    public ObjectStore<T> getStore() {
        return store;
    }

    public void setStore(ObjectStore<T> store) {
        this.store = store;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
}
