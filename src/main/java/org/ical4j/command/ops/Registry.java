package org.ical4j.command.ops;

import org.ical4j.command.vocab.Verb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The single table of operations. Every surface derives its interface from it and executes
 * through it; nothing dispatches around it.
 * <p>
 * Built once at startup. Registering the same (verb, subject, locus) tuple twice fails fast so a
 * duplicate can never shadow another definition.
 */
public final class Registry {

    private final Map<OperationId, Operation> operations;

    private Registry(Map<OperationId, Operation> operations) {
        this.operations = Collections.unmodifiableMap(new LinkedHashMap<>(operations));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Registry empty() {
        return new Registry(Map.of());
    }

    /** Operations in registration order. */
    public List<Operation> all() {
        return List.copyOf(operations.values());
    }

    public int size() {
        return operations.size();
    }

    public Optional<Operation> find(OperationId id) {
        return Optional.ofNullable(operations.get(id));
    }

    public Operation require(OperationId id) {
        return find(id).orElseThrow(() -> new OperationException.NotFound("No such operation: " + id));
    }

    /** Operations exposed to the given surface, in registration order. */
    public List<Operation> forSurface(Surface surface) {
        return operations.values().stream().filter(op -> op.exposedTo(surface)).toList();
    }

    /** Operations exposed to the surface, grouped by verb in {@link Verb} declaration order. */
    public Map<Verb, List<Operation>> byVerb(Surface surface) {
        Map<Verb, List<Operation>> grouped = new EnumMap<>(Verb.class);
        for (Operation op : forSurface(surface)) {
            grouped.computeIfAbsent(op.id().verb(), v -> new ArrayList<>()).add(op);
        }
        grouped.replaceAll((v, ops) -> List.copyOf(ops));
        return Collections.unmodifiableMap(grouped);
    }

    /** Executes an operation by identity after resolving its parameters. */
    public Result execute(OperationId id, Context context, Params params) {
        return require(id).execute(context, params);
    }

    public static final class Builder {
        private final Map<OperationId, Operation> operations = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder add(Operation operation) {
            Operation previous = operations.putIfAbsent(operation.id(), operation);
            if (previous != null) {
                throw new IllegalStateException("Operation already registered: " + operation.id());
            }
            return this;
        }

        public Builder addAll(Iterable<Operation> ops) {
            ops.forEach(this::add);
            return this;
        }

        public Registry build() {
            return new Registry(operations);
        }
    }
}
