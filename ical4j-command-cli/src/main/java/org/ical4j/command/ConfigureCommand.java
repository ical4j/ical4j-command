package org.ical4j.command;

import picocli.CommandLine;

@CommandLine.Command(name = "configure", description = "Configure settings")
public class ConfigureCommand extends AbstractCommand<Void> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
