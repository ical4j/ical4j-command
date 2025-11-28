package org.ical4j.command.collection;

import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.property.Uid;
import org.ical4j.command.collection.AbstractCollectionCommand;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStore;
import picocli.CommandLine;

import java.util.List;
import java.util.function.Consumer;

@CommandLine.Command(name = "import-cards", description = "Update an object collection")
public class ImportCards extends AbstractCollectionCommand<CardCollection, List<Uid>> {

    public ImportCards() {
    }

    public ImportCards(String collection, Consumer<List<Uid>> consumer) {
        super(collection, consumer);
    }

    public ImportCards(String collection, Consumer<List<Uid>> consumer, ObjectStore<CardCollection> store) {
        super(collection, consumer, store);
    }

    @Override
    public Integer call() {
        return 1;
    }
}
