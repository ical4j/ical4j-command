package org.ical4j.command.strategy;

import net.fortuna.ical4j.extensions.strategy.observance.Observance;
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

@CommandLine.Command(name = "observance", description = "Generate an observance")
public class CreateObservance extends AbstractCommand<Calendar> implements InputHandler {

    public enum ObservanceType {
        Anniversary, Observance
    }

    @CommandLine.Option(names = "--summary", interactive = true)
    private String summary;

    @CommandLine.Option(names = "--description", interactive = true)
    private String description;

    @CommandLine.Option(names = "--type", interactive = true, arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "Observance")
    private ObservanceType type;

    public CreateObservance() {
        super(DefaultOutputHandlers.VALIDATING_CALENDAR_PRINTER(new PrintWriter(System.out, true)));
    }

    public CreateObservance(Consumer<Calendar> consumer) {
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
        if (type == null && System.console() != null) {
            type = ObservanceType.valueOf(singleInput("Type"));
        }
        VEvent observance = new VEvent().add(new RandomUidGenerator().generateUid())
                .add(new Summary(summary))
                .add(new Description(description));
        switch (type) {
            default -> {
                observance = new Observance().withPrototype(observance).get();
            }
        }
        Calendar calendar = new Calendar().withDefaults()
                .withProperty(new ProdId("-//ical4j//iCal4j 4.0//EN"))
                .withComponent(observance).getFluentTarget();
        getOutputHandler().accept(calendar);
        return 0;
    }
}
