package org.ical4j.command.calendar


import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.ObjectStore
import spock.lang.Specification

class DeleteCalendarTest extends Specification {

    def 'test delete calendar'() {
        given: 'a mock calendar collection'
        ObjectStore store = Mock()
        CalendarCollection collection = Mock()
        store.getCollection(_) >> collection

        when: 'a delete calendar command is run'
        new DeleteCalendar('testCollection', store).withCalendarUid('1234').call()

        then: 'collection remove calendar is invoked'
        1 * collection.removeCalendar('1234')
    }
}
