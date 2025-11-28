package org.ical4j.command.collection

import org.ical4j.command.workspace.DeleteCollection
import org.ical4j.connector.ObjectCollection
import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class DeleteCollectionTest extends Specification {

    def 'test delete collection'() {
        given: 'a mock card store'
        ObjectStore store = Mock()
        ObjectCollection collection = Mock()

        when: 'a delete collection command is run'
        new DeleteCollection((c) -> {}, store).withCollectionName('testCollection').call()

        then: 'store remove collection is invoked'
        1 * store.getCollection('testCollection') >> collection
        1 * collection.delete()
    }
}
