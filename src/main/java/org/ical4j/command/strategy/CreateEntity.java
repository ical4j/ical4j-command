package org.ical4j.command.strategy;

import net.fortuna.ical4j.extensions.strategy.entity.Individual;
import net.fortuna.ical4j.extensions.strategy.entity.Organization;
import net.fortuna.ical4j.vcard.Entity;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Fn;
import net.fortuna.ical4j.vcard.property.Note;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.InputHandler;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.function.Consumer;

@CommandLine.Command(name = "entity", description = "Generate an entity")
public class CreateEntity extends AbstractCommand<VCard> implements InputHandler {

    public enum EntityType {
        Individual, Org, Group, Location
    }

    @CommandLine.Option(names = "--formatted-name", interactive = true)
    private String fn;

    @CommandLine.Option(names = "--note", interactive = true)
    private String note;

    @CommandLine.Option(names = "--type", interactive = true, arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "Individual")
    private EntityType type;

    public CreateEntity() {
        super(DefaultOutputHandlers.VALIDATING_VCARD_PRINTER(new PrintWriter(System.out, true)));
    }

    public CreateEntity(Consumer<VCard> consumer) {
        super(consumer);
    }

    @Override
    public Integer call() throws Exception {
        if (fn == null && System.console() != null) {
            fn = singleInput("Formatted Name");
        }
        if (note == null && System.console() != null) {
            note = multiInput("Note");
        }
        if (type == null && System.console() != null) {
            type = EntityType.valueOf(singleInput("Type"));
        }
        Entity entity = new Entity().add(new Fn(fn)).add(new Note(note));
        switch (type) {
            case Org -> {
                entity = new Organization().withPrototype(entity).get();
            }
            default -> {
                entity = new Individual().withPrototype(entity).get();
            }
        }
        VCard card = (VCard) new VCard().add(entity);
        getOutputHandler().accept(card);
        return 0;
    }
}
