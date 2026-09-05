package org.ical4j.command.config;

import org.ical4j.command.AbstractCommand;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "configure", description = "Configure settings")
public class ConfigureCommand extends AbstractCommand<String> {

    private static final List<String> CONFIG_SECTIONS = List.of("user", "workspace", "strategy", "editor",
            "filter", "remote");

    @CommandLine.Parameters(index = "0", description = "Setting to configure")
    private String setting;

    @CommandLine.Parameters(index = "1", description = "Setting value", arity = "1..*")
    private List<String> value;

    @Override
    public Integer call() throws Exception {
        if (!CONFIG_SECTIONS.contains(setting.split("\\.")[0])) {
            getOutputHandler().accept("Unknown setting: " + setting);
            return 1;
        }
        String previous = CommandConfig.INSTANCE.set(setting, String.join(" ", value));
        getOutputHandler().accept("Updated setting '" + setting + "' from '" + previous + "' to '" + String.join(" ", value) + "'");
        return 0;
    }
}
