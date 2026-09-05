package org.ical4j.command.collection


import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class AddCollectionTest extends Specification {

    def 'test create collection'() {
        given: 'a mock card store'
        ObjectStore store = Mock()

        when: 'a create collection command is run'
        new AddCollection((collection) -> {}, store).withCollectionName('testCollection').call()

        then: 'store add collection is invoked'
        1 * store.addCollection('testCollection')
    }
}
