package org.ical4j.command.cli;

import org.ical4j.command.config.CommandConfig;
import org.ical4j.command.ops.OperationException;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * {@code ict config [<key> [<value>...]]}: show all settings, show one, or set one. Not a
 * registry operation; it edits the configuration the registry's context is built from.
 */
final class ConfigCommand implements Callable<Integer> {

    /** Top-level TOML sections a setting may be written to. */
    static final List<String> SECTIONS = List.of("user", "workspace", "store", "contentType");

    private final Supplier<CommandConfig> config;

    private CommandSpec spec;

    private ConfigCommand(Supplier<CommandConfig> config) {
        this.config = config;
    }

    static CommandLine create(Supplier<CommandConfig> config) {
        ConfigCommand command = new ConfigCommand(config);
        CommandSpec spec = CommandSpec.wrapWithoutInspection(command).name("config");
        spec.usageMessage().description("Show or change settings in the configuration file",
                "With no arguments, list every setting; with a key, show it; with a key and value, set it.");
        spec.addPositional(PositionalParamSpec.builder().index("0").arity("0..1").paramLabel("<key>")
                .type(String.class).description("Setting, as section.name (sections: " + String.join(", ", SECTIONS) + ")")
                .build());
        spec.addPositional(PositionalParamSpec.builder().index("1..*").arity("0..*").paramLabel("<value>")
                .type(String[].class).description("New value; several words are joined with spaces").build());
        command.spec = spec;
        return new CommandLine(spec);
    }

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        String key = spec.positionalParameters().get(0).getValue();
        String[] value = spec.positionalParameters().get(1).getValue();
        CommandConfig cfg = config.get();

        if (key == null) {
            for (Map.Entry<String, String> e : cfg.entries().entrySet()) {
                out.println(e.getKey() + " = " + e.getValue());
            }
        } else if (value == null || value.length == 0) {
            String current = cfg.get(key, null);
            if (current == null) {
                throw new OperationException.NotFound("No such setting: " + key);
            }
            out.println(current);
        } else {
            if (!SECTIONS.contains(key.split("\\.")[0]) || !key.contains(".")) {
                throw new OperationException.NotFound("Unknown setting: " + key + " (sections: "
                        + String.join(", ", SECTIONS) + ")");
            }
            String joined = String.join(" ", value);
            String previous = cfg.set(key, joined);
            out.println(previous == null ? "Set '" + key + "' to '" + joined + "'"
                    : "Updated '" + key + "' from '" + previous + "' to '" + joined + "'");
        }
        out.flush();
        return 0;
    }
}
