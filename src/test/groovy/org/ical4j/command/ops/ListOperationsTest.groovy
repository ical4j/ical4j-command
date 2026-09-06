package org.ical4j.command.ops

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static org.ical4j.command.ops.Fixtures.*

class ListOperationsTest extends Specification {

    @TempDir
    Path dir

    Context ctx

    def setup() {
        ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(name: 'work'))
        op('add collection in workspace').execute(ctx, Params.of(name: 'people', type: 'card'))
        op('import calendar in collection').execute(ctx, Params.of(collection: 'work', file: write(dir, 'e1.ics', event('e1@ict', 'Standup', '20260910T090000Z')).toString()))
        op('import calendar in collection').execute(ctx, Params.of(collection: 'work', file: write(dir, 'e2.ics', event('e2@ict', 'Review', '20260911T090000Z')).toString()))
        op('import calendar in collection').execute(ctx, Params.of(collection: 'work', file: write(dir, 't1.ics', todo('t1@ict', 'Write spec')).toString()))
        op('import card in collection').execute(ctx, Params.of(collection: 'people', file: write(dir, 'c1.vcf', card('c1@ict', 'Ada Lovelace')).toString()))
    }

    def "'list event in collection' describes only the events"() {
        when:
        def result = op('list event in collection').execute(ctx, Params.of(collection: 'work')) as Result.Descriptors

        then:
        result.items()*.attributes()*.uid as Set == ['e1@ict', 'e2@ict'] as Set
        result.items().find { it.attributes().uid == 'e1@ict' }.attributes() == [uid: 'e1@ict', summary: 'Standup', start: '20260910T090000Z']
    }

    def "'list action in collection' describes only the to-dos"() {
        when:
        def result = op('list action in collection').execute(ctx, Params.of(collection: 'work')) as Result.Descriptors

        then:
        result.items()*.attributes()*.uid == ['t1@ict']
        result.items()[0].attributes().summary == 'Write spec'
    }

    def "'list note in collection' is empty when there are no journals"() {
        expect:
        (op('list note in collection').execute(ctx, Params.of(collection: 'work')) as Result.Descriptors).items().empty
    }

    def "'list entity in collection' describes the cards"() {
        when:
        def result = op('list entity in collection').execute(ctx, Params.of(collection: 'people')) as Result.Descriptors

        then:
        result.items()*.attributes() == [[uid: 'c1@ict', name: 'Ada Lovelace']]
    }

    def 'listing an unknown collection is a not-found error'() {
        when:
        op('list event in collection').execute(ctx, Params.of(collection: 'nope'))

        then:
        thrown(OperationException.NotFound)
    }
}
