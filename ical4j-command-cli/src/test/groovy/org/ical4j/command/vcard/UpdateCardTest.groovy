package org.ical4j.command.vcard


import net.fortuna.ical4j.vcard.VCard
import org.ical4j.command.collection.UpdateCard
import org.ical4j.connector.CardCollection
import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class UpdateCardTest extends Specification {

    def 'test update card'() {
        given: 'a mock card collection'
        ObjectStore store = Mock()
        CardCollection collection = Mock()

        and: 'a vcard instance'
        VCard card = []

        when: 'an update card command is run'
        new UpdateCard('default', store).withCard(card).call()

        then: 'collection merge is invoked'
        1 * store.getCollection('default') >> collection
        1 * collection.merge(card)
    }
}
