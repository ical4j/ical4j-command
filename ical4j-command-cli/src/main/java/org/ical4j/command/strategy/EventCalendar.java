package org.ical4j.command.strategy;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.Summary;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.InputHandler;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.function.Consumer;

@CommandLine.Command(name = "event", description = "Create a new event calendar object")
public class EventCalendar extends AbstractCommand<Calendar> implements InputHandler {

    @CommandLine.Option(names = "--summary", interactive = true)
    private String summary;

    @CommandLine.Option(names = "--description", interactive = true)
    private String description;

    public EventCalendar() {
        super(DefaultOutputHandlers.VALIDATING_CALENDAR_PRINTER(new PrintWriter(System.out, true)));
    }

    public EventCalendar(Consumer<Calendar> consumer) {
        super(consumer);
    }

    @Override
    public Integer call() throws Exception {
        if (summary == null && System.console() != null) {
            summary = singleInput("Summary");
        }
        if (description == null && System.console() != null) {
            description = multiInput("Description");
        }
        Calendar calendar = new Calendar().withDefaults()
                .withProperty(new DtStamp())
                .withProperty(new Summary(summary))
                .withProperty(new Description(description)).getFluentTarget();
        getOutputHandler().accept(calendar);
        return 0;
    }
}
