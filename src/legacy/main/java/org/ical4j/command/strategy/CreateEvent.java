package org.ical4j.command.strategy;

import net.fortuna.ical4j.extensions.strategy.event.Meeting;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.InputHandler;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.function.Consumer;

@CommandLine.Command(name = "event", description = "Generate an event")
public class CreateEvent extends AbstractCommand<Calendar> implements InputHandler {

    @CommandLine.Option(names = "--summary", interactive = true)
    private String summary;

    @CommandLine.Option(names = "--description", interactive = true)
    private String description;

    public CreateEvent() {
        super(DefaultOutputHandlers.VALIDATING_CALENDAR_PRINTER(new PrintWriter(System.out, true)));
    }

    public CreateEvent(Consumer<Calendar> consumer) {
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
        VEvent meeting = new Meeting().withPrototype(new VEvent().add(new Summary(summary))
                .add(new Description(description))
                .add(new RandomUidGenerator().generateUid())).get();
        Calendar calendar = new Calendar().withDefaults()
                .withProperty(new ProdId("-//ical4j//iCal4j 4.0//EN"))
                .withComponent(meeting).getFluentTarget();
        getOutputHandler().accept(calendar);
        return 0;
    }
}
