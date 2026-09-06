package org.ical4j.command.ops;

import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.command.vocab.Locus;
import org.ical4j.command.vocab.SerializationFamily;
import org.ical4j.command.vocab.Verb;
import org.ical4j.connector.CardCollection;

import java.util.Arrays;
import java.util.List;

/**
 * Moving vCard data into and out of a collection.
 */
final class CardOperations {

    private static final Param UID = Param.string("uid", "UID of one stored card");

    private CardOperations() {
    }

    static List<Operation> all() {
        return List.of(importCard(), exportCard(), removeCard());
    }

    static Operation importCard() {
        return Operation.define(Verb.IMPORT, SerializationFamily.CARD, Locus.COLLECTION)
                .description("Import vCard data into a collection, merging by UID")
                .params(Stores.COLLECTION, Inputs.FILE)
                .handler((context, params) -> {
                    CardCollection collection = Stores.cardCollection(context, params.require("collection"));
                    VCard card = Inputs.card(context, params);
                    Uid[] uids = Stores.call(() -> collection.merge(card));
                    return new Result.Identifiers(Arrays.stream(uids).map(Uid::getValue).toList());
                })
                .build();
    }

    static Operation exportCard() {
        return Operation.define(Verb.EXPORT, SerializationFamily.CARD, Locus.COLLECTION)
                .description("Export one stored card by UID, or the whole collection as one vCard")
                .params(Stores.COLLECTION, UID)
                .handler((context, params) -> {
                    CardCollection collection = Stores.cardCollection(context, params.require("collection"));
                    VCard card = params.string("uid").isPresent()
                            ? Stores.call(() -> collection.getCard(params.require("uid")))
                            : Stores.call(collection::export);
                    if (card == null) {
                        throw new OperationException.NotFound("No such card: " + params.string("uid", ""));
                    }
                    return new Result.CardData(card);
                })
                .build();
    }

    static Operation removeCard() {
        return Operation.define(Verb.REMOVE, SerializationFamily.CARD, Locus.COLLECTION)
                .description("Remove one stored card by UID")
                .params(Stores.COLLECTION, UID.asRequired())
                .handler((context, params) -> {
                    CardCollection collection = Stores.cardCollection(context, params.require("collection"));
                    String uid = params.require("uid");
                    // The connector's removeCard(uid) indexes into an empty list when the UID is
                    // unknown, so remove through removeAll and inspect the result.
                    List<VCard> removed = Stores.call(() -> collection.removeAll(uid));
                    if (removed.isEmpty()) {
                        throw new OperationException.NotFound("No such card: " + uid);
                    }
                    return new Result.Status("Removed card '" + uid + "'");
                })
                .build();
    }
}
