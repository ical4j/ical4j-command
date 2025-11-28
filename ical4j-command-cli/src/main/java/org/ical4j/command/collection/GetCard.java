package org.ical4j.command.collection;

import net.fortuna.ical4j.vcard.VCard;
import org.ical4j.connector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(name = "get-card", description = "Retrieve an existing card")
public class GetCard extends AbstractCollectionCommand<CardCollection, VCard> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCard.class);

    @CommandLine.Option(names = {"-uid"})
    private String cardUid;

    public GetCard() {
        super();
    }

    public GetCard(String collectionName, Consumer<VCard> consumer) {
        super(collectionName, consumer);
    }

    public GetCard(String collectionName, Consumer<VCard> consumer, ObjectStore<CardCollection> store) {
        super(collectionName, consumer, store);
    }

    public GetCard withCardUid(String cardUid) {
        this.cardUid = cardUid;
        return this;
    }

    @Override
    public Integer call() {
        try {
            getOutputHandler().accept(getCollection().getCard(cardUid));
        } catch (FailedOperationException | ObjectNotFoundException | ObjectStoreException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
