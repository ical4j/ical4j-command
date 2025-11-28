package org.ical4j.command.collection;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.ical4j.command.DefaultOutputHandlers.STDOUT_LIST_PRINTER;
import static org.ical4j.connector.ObjectCollection.DEFAULT_COLLECTION;

@CommandLine.Command(name = "list-cards", description = "List all existing cards")
public class ListCards extends AbstractCollectionCommand<CardCollection, List<VCard>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCards.class);

    public ListCards() {
        super(DEFAULT_COLLECTION, STDOUT_LIST_PRINTER());
    }

    public ListCards(String collectionName, Consumer<List<VCard>> consumer) {
        super(collectionName, consumer);
    }

    @Override
    public Integer call() {
        try {
            List<VCard> cards = new ArrayList<>();
            for (String uid : getCollection().listObjectUIDs()) {
                cards.add(getCollection().getCard(uid));
            }
            getOutputHandler().accept(cards);
        } catch (ObjectStoreException | ObjectNotFoundException | FailedOperationException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
