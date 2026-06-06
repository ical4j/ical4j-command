package org.ical4j.command.strategy;

import net.fortuna.ical4j.extensions.model.concept.NoteType;
import net.fortuna.ical4j.extensions.strategy.note.Note;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.InputHandler;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.function.Consumer;

@CommandLine.Command(name = "note", description = "Generate a note")
public class CreateNote extends AbstractCommand<Calendar> implements InputHandler {

//    public enum NoteType {
//        Decision, Note, Objective, Risk
//    }

    @CommandLine.Option(names = "--summary", interactive = true)
    private String summary;

    @CommandLine.Option(names = "--description", interactive = true)
    private String description;

    @CommandLine.Option(names = "--type", interactive = true, arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "Note")
    private NoteType.Id type;

    public CreateNote() {
        super(DefaultOutputHandlers.VALIDATING_CALENDAR_PRINTER(new PrintWriter(System.out, true)));
    }

    public CreateNote(Consumer<Calendar> consumer) {
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
            type = NoteType.Id.valueOf(singleInput("Type"));
        }
        VJournal note = new VJournal().add(new RandomUidGenerator().generateUid())
                .add(new Summary(summary))
                .add(new Description(description));
        switch (type) {
            default -> {
                note = new Note().title(summary).date(LocalDate.now())
                        .withPrototype(note).get();
            }
        }
        Calendar calendar = new Calendar().withDefaults()
                .withProperty(new ProdId("-//ical4j//iCal4j 4.0//EN"))
                .withComponent(note).getFluentTarget();
        getOutputHandler().accept(calendar);
        return 0;
    }
}
