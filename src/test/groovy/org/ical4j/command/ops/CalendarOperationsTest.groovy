package org.ical4j.command.ops

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static org.ical4j.command.ops.Fixtures.*

class CalendarOperationsTest extends Specification {

    @TempDir
    Path dir

    Context ctx

    def setup() {
        ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(collection: 'events'))
    }

    def "'import calendar in collection' merges a file and reports the UIDs"() {
        given:
        def file = write(dir, 'a.ics', event('a@ict', 'First'))

        when:
        def result = op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: file.toString()))

        then:
        (result as Result.Identifiers).ids() == ['a@ict']
        ctx.calendarStore().getCollection('events', 'work').listObjectUIDs() == ['a@ict']

        when:
        op('import calendar in collection').execute(ctx, Params.of(collection: 'missing', file: file.toString()))

        then:
        thrown(OperationException.NotFound)

        when:
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: dir.resolve('none.ics').toString()))

        then:
        thrown(OperationException.NotFound)
    }

    def "'import calendar in collection' reads standard input when no file is given"() {
        given:
        def stdin = Context.builder().activeWorkspace('work')
                .calendarStore(ctx.calendarStore()).cardStore(ctx.cardStore())
                .stdin({ new ByteArrayInputStream(event('b@ict', 'Second').bytes) }).build()

        when:
        def result = op('import calendar in collection').execute(stdin, Params.of(collection: 'events'))

        then:
        (result as Result.Identifiers).ids() == ['b@ict']
    }

    def "'import calendar in collection' rejects data that does not parse"() {
        given:
        def file = write(dir, 'bad.ics', 'not a calendar')

        when:
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: file.toString()))

        then:
        thrown(OperationException.InvalidData)
    }

    def "'export calendar in collection' returns one object by UID or the whole collection"() {
        given:
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: write(dir, 'a.ics', event('a@ict', 'First')).toString()))
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: write(dir, 'b.ics', event('b@ict', 'Second')).toString()))

        when:
        def one = op('export calendar in collection').execute(ctx, Params.of(collection: 'events', uid: 'a@ict')) as Result.CalendarData

        then:
        one.calendar().getComponents()*.getUid()*.get()*.value == ['a@ict']

        when:
        def all = op('export calendar in collection').execute(ctx, Params.of(collection: 'events')) as Result.CalendarData

        then:
        all.calendar().getComponents()*.getUid()*.get()*.value as Set == ['a@ict', 'b@ict'] as Set

        when:
        op('export calendar in collection').execute(ctx, Params.of(collection: 'events', uid: 'nope'))

        then:
        thrown(OperationException.NotFound)
    }

    def "'export calendar in collection' adds a VTIMEZONE for each referenced TZID that lacks one"() {
        given:
        def ics = event('tz@ict', 'Melbourne').replace('DTSTART:20260910T100000Z', 'DTSTART;TZID=Australia/Melbourne:20260910T100000')
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: write(dir, 'tz.ics', ics).toString()))

        when:
        def calendar = (op('export calendar in collection').execute(ctx, Params.of(collection: 'events', uid: 'tz@ict')) as Result.CalendarData).calendar()
        def zones = calendar.getComponents('VTIMEZONE')

        then:
        zones*.getProperty('TZID')*.get()*.value == ['Australia/Melbourne']

        and: 'a calendar that already carries its zones is unchanged'
        CalendarOperations.withTimeZones(calendar).is(calendar)
    }

    def "'import calendar in collection' accepts several objects that reference a TZID with no VTIMEZONE"() {
        given: 'the connector alone fails on this input with a NullPointerException'
        def ics = event('tz@ict', 'Melbourne').replace('DTSTART:20260910T100000Z', 'DTSTART;TZID=Australia/Melbourne:20260910T100000')
                .replace('END:VCALENDAR', todo('t@ict', 'Todo').readLines()[3..-2].join('\n') + '\nEND:VCALENDAR')

        when:
        def result = op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: write(dir, 'two.ics', ics).toString()))

        then:
        (result as Result.Identifiers).ids() as Set == ['tz@ict', 't@ict'] as Set
    }

    def "'remove calendar in collection' removes one object by UID"() {
        given:
        op('import calendar in collection').execute(ctx, Params.of(collection: 'events', file: write(dir, 'a.ics', event('a@ict', 'First')).toString()))

        when:
        def result = op('remove calendar in collection').execute(ctx, Params.of(collection: 'events', uid: 'a@ict'))

        then:
        (result as Result.Status).message() == "Removed calendar 'a@ict'"
        ctx.calendarStore().getCollection('events', 'work').listObjectUIDs() == []

        when:
        op('remove calendar in collection').execute(ctx, Params.of(collection: 'events', uid: 'a@ict'))

        then:
        thrown(OperationException.NotFound)

        when:
        op('remove calendar in collection').execute(ctx, Params.of(collection: 'events'))

        then:
        thrown(OperationException.Usage)
    }
}
