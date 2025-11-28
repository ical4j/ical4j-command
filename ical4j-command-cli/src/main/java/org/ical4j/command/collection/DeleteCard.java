package org.ical4j.command.collection;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "delete-card", description = "Delete an existing card")
public class DeleteCard extends AbstractCollectionCommand<CardCollection, VCard> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCard.class);

    @CommandLine.Option(names = {"-uid"})
    private String cardUid;

    public DeleteCard() {
        super();
    }

    public DeleteCard(String collectionName, Consumer<VCard> consumer) {
        super(collectionName, consumer);
    }

    public DeleteCard(String collectionName, Consumer<VCard> consumer, ObjectStore<CardCollection> store) {
        super(collectionName, consumer, store);
    }

    public DeleteCard withCardUid(String cardUid) {
        this.cardUid = cardUid;
        return this;
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getCollection().removeCard(cardUid));
        } catch (FailedOperationException | ObjectNotFoundException | ObjectStoreException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
