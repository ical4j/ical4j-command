package org.ical4j.command.card;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.InputOptions;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class AbstractEntityCommand<T> extends AbstractCommand<T> {

    @CommandLine.ArgGroup(multiplicity = "1")
    protected InputOptions input;

    private VCard card;

    public AbstractEntityCommand() {
    }

    public AbstractEntityCommand(Consumer<T> consumer) {
        super(consumer);
    }

    public AbstractEntityCommand<T> withCard(VCard card) {
        this.card = card;
        return this;
    }

    public VCard getCard() throws ParserException, IOException, ObjectStoreException, ObjectNotFoundException {
        if (card == null) {
            card = input.toVCard();
        }
        return card;
    }
}
