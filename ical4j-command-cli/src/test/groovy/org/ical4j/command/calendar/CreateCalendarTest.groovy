package org.ical4j.command.calendar

import net.fortuna.ical4j.model.Calendar
import org.ical4j.command.collection.CreateCalendar
import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CalendarStore
import spock.lang.Specification

class CreateCalendarTest extends Specification {

    def 'test create calendar'() {
        given: 'a mock calendar collection'
        CalendarStore store = Mock()
        CalendarCollection collection = Mock()
        store.getCollection('testCollection') >> collection

        and: 'a calendar instance'
        Calendar calendar = []

        when: 'a create calendar command is run'
        new CreateCalendar(store)
                .withCalendar(calendar)
                .withCollectionName('testCollection').call()

        then: 'collection add calendar is invoked'
        1 * collection.add(calendar)
    }
}
