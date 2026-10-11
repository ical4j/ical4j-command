package org.ical4j.command.ops;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parsed parameter values handed to a handler, keyed by {@link Param#name()}.
 * <p>
 * Surfaces populate this from their own parse results. Values are stored in their string form
 * (or a list of strings for multiple parameters) so the conversion rules live in one place.
 */
public final class Params {

    private final Map<String, Object> values;

    private Params(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    public static Params empty() {
        return new Params(Map.of());
    }

    public static Params of(Map<String, ?> values) {
        return new Params(new HashMap<>(values));
    }

    /**
     * Applies defaults from the descriptors and checks required parameters.
     *
     * @throws OperationException.Usage if a required parameter is missing or an enum value is not a choice
     */
    public Params resolve(List<Param> descriptors) {
        Map<String, Object> resolved = new HashMap<>(values);
        for (Param p : descriptors) {
            Object v = resolved.get(p.name());
            if (v == null && p.defaultValue() != null) {
                resolved.put(p.name(), p.multiple() ? List.of(p.defaultValue()) : p.defaultValue());
                v = resolved.get(p.name());
            }
            if (v == null && p.required()) {
                throw new OperationException.Usage("Missing required parameter: " + p.name());
            }
            if (v != null && p.type() == ParamType.ENUM) {
                for (String s : p.multiple() ? strings(p.name()) : List.of(v.toString())) {
                    if (!p.choices().contains(s)) {
                        throw new OperationException.Usage("Invalid value '" + s + "' for " + p.name()
                                + "; expected one of " + p.choices());
                    }
                }
            }
        }
        return new Params(resolved);
    }

    public boolean has(String name) {
        return values.get(name) != null;
    }

    public Optional<String> string(String name) {
        Object v = values.get(name);
        if (v instanceof List<?> l) {
            return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0).toString());
        }
        return Optional.ofNullable(v).map(Object::toString);
    }

    public String string(String name, String fallback) {
        return string(name).orElse(fallback);
    }

    public String require(String name) {
        return string(name).orElseThrow(() -> new OperationException.Usage("Missing required parameter: " + name));
    }

    public List<String> strings(String name) {
        Object v = values.get(name);
        if (v == null) {
            return List.of();
        }
        if (v instanceof List<?> l) {
            return l.stream().map(Object::toString).toList();
        }
        return List.of(v.toString());
    }

    public Optional<Integer> integer(String name) {
        return string(name).map(s -> {
            try {
                return Integer.valueOf(s.trim());
            } catch (NumberFormatException e) {
                throw new OperationException.Usage("Expected an integer for " + name + ": " + s);
            }
        });
    }

    public boolean bool(String name) {
        return string(name).map(s -> s.equalsIgnoreCase("true") || s.equals("1")).orElse(false);
    }

    public Optional<Path> path(String name) {
        return string(name).map(Path::of);
    }

    public Map<String, Object> asMap() {
        return values;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
