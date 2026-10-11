package org.ical4j.command.ops;

import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.Subject;
import org.ical4j.command.vocab.Verb;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * One registry entry: identity, parameters, handler, and which surfaces expose it.
 *
 * @param id          the (verb, subject, locus) tuple
 * @param description one sentence, used for CLI help and the MCP tool description
 * @param params      ordered parameter descriptors, names unique
 * @param exposure    surfaces that expose this operation
 * @param handler     the implementation
 */
public record Operation(OperationId id, String description, List<Param> params, Set<Surface> exposure, Handler handler) {

    public Operation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(handler, "handler");
        params = List.copyOf(params);
        exposure = exposure.isEmpty() ? EnumSet.noneOf(Surface.class) : EnumSet.copyOf(exposure);
        Set<String> names = new HashSet<>();
        for (Param p : params) {
            if (!names.add(p.name())) {
                throw new IllegalArgumentException("Duplicate parameter '" + p.name() + "' on " + id);
            }
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Operation " + id + " needs a description");
        }
    }

    public static Builder define(Verb verb, Subject subject, Locus locus) {
        return new Builder(OperationId.of(verb, subject, locus));
    }

    public boolean exposedTo(Surface surface) {
        return exposure.contains(surface);
    }

    /** Resolves defaults and required checks, then runs the handler. */
    public Result execute(Context context, Params params) {
        return handler.apply(context, params.resolve(this.params));
    }

    public static final class Builder {
        private final OperationId id;
        private String description = "";
        private final List<Param> params = new ArrayList<>();
        private final Set<Surface> exposure = EnumSet.allOf(Surface.class);
        private Handler handler;

        private Builder(OperationId id) {
            this.id = id;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder param(Param param) {
            params.add(param);
            return this;
        }

        public Builder params(Param... params) {
            this.params.addAll(List.of(params));
            return this;
        }

        /** Restricts exposure; the default is every surface. */
        public Builder only(Surface... surfaces) {
            exposure.clear();
            exposure.addAll(List.of(surfaces));
            return this;
        }

        public Builder handler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public Operation build() {
            return new Operation(id, description, params, exposure, handler);
        }
    }
}
