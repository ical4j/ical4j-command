package org.ical4j.command.ops

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static org.ical4j.command.ops.Fixtures.context
import static org.ical4j.command.ops.Fixtures.op

class CollectionOperationsTest extends Specification {

    @TempDir
    Path dir

    def "'add collection in workspace' creates a collection of the requested type once"() {
        given:
        def ctx = context(dir)

        when:
        def result = op('add collection in workspace').execute(ctx, Params.of(name: 'events'))

        then:
        (result as Result.Status).message() == "Added calendar collection 'events'"
        dir.resolve('calendars/work/events').toFile().directory

        when:
        op('add collection in workspace').execute(ctx, Params.of(name: 'people', type: 'card'))

        then:
        dir.resolve('vcards/work/people').toFile().directory

        when:
        op('add collection in workspace').execute(ctx, Params.of(name: 'events'))

        then:
        thrown(OperationException.AlreadyExists)

        when:
        op('add collection in workspace').execute(ctx, Params.empty())

        then:
        thrown(OperationException.Usage)
    }

    def "'get collection in workspace' describes a collection and reports missing ones"() {
        given:
        def ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(name: 'events'))

        when:
        def result = op('get collection in workspace').execute(ctx, Params.of(name: 'events')) as Result.Descriptor

        then:
        result.attributes().name == 'events'
        result.attributes().type == 'calendar'
        result.attributes().objects == '0'

        when:
        op('add collection in workspace').execute(ctx, Params.of(name: 'notes', description: 'Scratch notes'))

        then:
        (op('get collection in workspace').execute(ctx, Params.of(name: 'notes')) as Result.Descriptor).attributes().description == 'Scratch notes'

        when:
        op('get collection in workspace').execute(ctx, Params.of(name: 'missing'))

        then:
        thrown(OperationException.NotFound)
    }

    def "'remove collection in workspace' deletes the collection directory"() {
        given:
        def ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(name: 'events'))

        when:
        def result = op('remove collection in workspace').execute(ctx, Params.of(name: 'events'))

        then:
        (result as Result.Status).message() == "Removed calendar collection 'events'"
        !dir.resolve('calendars/work/events').toFile().exists()

        when:
        op('remove collection in workspace').execute(ctx, Params.of(name: 'events'))

        then:
        thrown(OperationException.NotFound)
    }

    def "'list collection in workspace' lists calendar and card collections of the active workspace only"() {
        given:
        def ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(name: 'events'))
        op('add collection in workspace').execute(ctx, Params.of(name: 'people', type: 'card'))
        op('add collection in workspace').execute(context(dir, 'other'), Params.of(name: 'elsewhere'))

        when:
        def result = op('list collection in workspace').execute(ctx, Params.empty()) as Result.Descriptors

        then:
        result.items()*.attributes()*.name as Set == ['events', 'people'] as Set
        result.items().find { it.attributes().name == 'people' }.attributes().type == 'card'
    }
}
