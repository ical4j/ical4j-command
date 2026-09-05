package org.ical4j.command.ops;

import java.util.List;
import java.util.Objects;

/**
 * A surface-neutral parameter descriptor.
 *
 * @param name         the parameter name; the CLI exposes {@code --name}, MCP a property {@code name}
 * @param type         the value type
 * @param required     whether a value must be supplied when no default exists
 * @param defaultValue the default as a string, or {@code null}
 * @param description  help text
 * @param multiple     whether the parameter accepts a list of values
 * @param choices      the permitted values for {@link ParamType#ENUM}, otherwise empty
 */
public record Param(String name, ParamType type, boolean required, String defaultValue, String description,
                    boolean multiple, List<String> choices) {

    public Param {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
        choices = choices == null ? List.of() : List.copyOf(choices);
        if (!name.matches("[a-z][a-z0-9-]*")) {
            throw new IllegalArgumentException("Parameter name must be lowercase kebab-case: " + name);
        }
        if (type == ParamType.ENUM && choices.isEmpty()) {
            throw new IllegalArgumentException("Enum parameter needs choices: " + name);
        }
        if (type != ParamType.ENUM && !choices.isEmpty()) {
            throw new IllegalArgumentException("Only enum parameters take choices: " + name);
        }
        if (defaultValue != null && type == ParamType.ENUM && !choices.contains(defaultValue)) {
            throw new IllegalArgumentException("Default is not one of the choices: " + name);
        }
        if (required && defaultValue != null) {
            throw new IllegalArgumentException("A parameter with a default is not required: " + name);
        }
    }

    public static Param string(String name, String description) {
        return new Param(name, ParamType.STRING, false, null, description, false, List.of());
    }

    public static Param integer(String name, String description) {
        return new Param(name, ParamType.INTEGER, false, null, description, false, List.of());
    }

    public static Param bool(String name, String description) {
        return new Param(name, ParamType.BOOLEAN, false, "false", description, false, List.of());
    }

    public static Param path(String name, String description) {
        return new Param(name, ParamType.PATH, false, null, description, false, List.of());
    }

    public static Param choice(String name, String description, List<String> choices) {
        return new Param(name, ParamType.ENUM, false, null, description, false, choices);
    }

    public static Param choice(String name, String description, Class<? extends Enum<?>> choices) {
        return choice(name, description, java.util.Arrays.stream(choices.getEnumConstants())
                .map(c -> c.name().toLowerCase(java.util.Locale.ROOT)).toList());
    }

    public Param asRequired() {
        return new Param(name, type, true, null, description, multiple, choices);
    }

    public Param defaultTo(String value) {
        return new Param(name, type, false, value, description, multiple, choices);
    }

    public Param asMultiple() {
        return new Param(name, type, required, defaultValue, description, true, choices);
    }
}
