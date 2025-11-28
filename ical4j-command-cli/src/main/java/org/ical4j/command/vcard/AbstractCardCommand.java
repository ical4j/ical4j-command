package org.ical4j.command.vcard;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.InputOptions;
import picocli.CommandLine;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class AbstractCardCommand<T> extends AbstractCommand<T> {

    @CommandLine.ArgGroup(multiplicity = "1")
    protected InputOptions input;

    private VCard card;

    public AbstractCardCommand() {
    }

    public AbstractCardCommand(Consumer<T> consumer) {
        super(consumer);
    }

    public AbstractCardCommand<T> withCard(VCard card) {
        this.card = card;
        return this;
    }

    public VCard getCard() throws ParserException, IOException {
        if (card == null) {
            card = input.toVCard();
        }
        return card;
    }
}
