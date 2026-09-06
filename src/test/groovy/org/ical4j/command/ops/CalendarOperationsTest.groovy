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
        op('add collection in workspace').execute(ctx, Params.of(name: 'events'))
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
