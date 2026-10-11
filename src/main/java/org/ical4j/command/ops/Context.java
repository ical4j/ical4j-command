package org.ical4j.command.ops;

import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStore;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * What a handler may touch: the stores, the active workspace and standard input. Surfaces build
 * one from configuration; specs build one around in-memory or temp-dir stores. Handlers never
 * read the configuration singleton.
 */
public final class Context {

    private final String activeWorkspace;
    private final Supplier<ObjectStore<? extends CalendarCollection>> calendarStore;
    private final Supplier<ObjectStore<? extends CardCollection>> cardStore;
    private final Supplier<InputStream> stdin;

    private Context(Builder b) {
        this.activeWorkspace = Objects.requireNonNull(b.activeWorkspace, "activeWorkspace");
        this.calendarStore = b.calendarStore;
        this.cardStore = b.cardStore;
        this.stdin = b.stdin;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String activeWorkspace() {
        return activeWorkspace;
    }

    public ObjectStore<? extends CalendarCollection> calendarStore() {
        if (calendarStore == null) {
            throw new OperationException("No calendar store is configured");
        }
        return calendarStore.get();
    }

    public ObjectStore<? extends CardCollection> cardStore() {
        if (cardStore == null) {
            throw new OperationException("No card store is configured");
        }
        return cardStore.get();
    }

    public InputStream stdin() {
        return stdin == null ? InputStream.nullInputStream() : stdin.get();
    }

    public static final class Builder {
        private String activeWorkspace = "default";
        private Supplier<ObjectStore<? extends CalendarCollection>> calendarStore;
        private Supplier<ObjectStore<? extends CardCollection>> cardStore;
        private Supplier<InputStream> stdin;

        public Builder activeWorkspace(String workspace) {
            this.activeWorkspace = workspace;
            return this;
        }

        public Builder calendarStore(ObjectStore<? extends CalendarCollection> store) {
            this.calendarStore = () -> store;
            return this;
        }

        public Builder calendarStore(Supplier<ObjectStore<? extends CalendarCollection>> store) {
            this.calendarStore = store;
            return this;
        }

        public Builder cardStore(ObjectStore<? extends CardCollection> store) {
            this.cardStore = () -> store;
            return this;
        }

        public Builder cardStore(Supplier<ObjectStore<? extends CardCollection>> store) {
            this.cardStore = store;
            return this;
        }

        public Builder stdin(Supplier<InputStream> stdin) {
            this.stdin = stdin;
            return this;
        }

        public Context build() {
            return new Context(this);
        }
    }
}
