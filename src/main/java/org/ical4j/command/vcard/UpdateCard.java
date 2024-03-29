package org.ical4j.command.vcard;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.io.IOException;
import java.util.function.Consumer;

@CommandLine.Command(name = "update", description = "Update an existing card")
public class UpdateCard extends AbstractCardCommand<Uid[]> {

    public UpdateCard() {
        super();
    }

    public UpdateCard(String collectionName, Consumer<Uid[]> consumer) {
        super(collectionName, consumer);
    }

    public UpdateCard(String collectionName, ObjectStore<CardCollection> store) {
        super(collectionName, store);
    }

    @Override
    public Integer call() {
        try {
            getConsumer().accept(getCollection().merge(getCard()));
        } catch (ObjectStoreException | ConstraintViolationException | ObjectNotFoundException | IOException |
                 ParserException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
