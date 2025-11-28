package org.ical4j.command.vcard

import org.ical4j.command.collection.GetCard
import org.ical4j.connector.CardCollection
import org.ical4j.connector.CardStore
import spock.lang.Specification

class GetCardTest extends Specification {

    def 'test get card'() {
        given: 'a mock card collection'
        CardStore store = Mock()
        CardCollection collection = Mock()

        when: 'a get card command is run'
        new GetCard('default', (card) -> {}, store).withCardUid('1234').call()

        then: 'collection get card is invoked'
        1 * store.getCollection('default') >> collection
        1 * collection.getCard('1234')
    }
}
