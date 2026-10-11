package org.ical4j.command.ops

import org.ical4j.command.vocab.EntityType
import org.ical4j.command.vocab.Locus
import org.ical4j.command.vocab.SerializationFamily
import org.ical4j.command.vocab.Verb
import spock.lang.Specification

class OperationTest extends Specification {

    def 'identity renders verb-first and allows every kind of subject'() {
        expect:
        OperationId.of(verb, subject, locus).toString() == text

        where:
        verb          | subject                     | locus            | text
        Verb.LIST     | EntityType.EVENT            | Locus.COLLECTION | 'list event in collection'
        Verb.ADD      | Locus.COLLECTION            | Locus.WORKSPACE  | 'add collection in workspace'
        Verb.IMPORT   | SerializationFamily.CALENDAR | Locus.COLLECTION | 'import calendar in collection'
        Verb.GENERATE | EntityType.EVENT            | Locus.NONE       | 'generate event'
        Verb.SEND     | SerializationFamily.CALENDAR | Locus.CHANNEL    | 'send calendar in channel'
        Verb.LIST     | Locus.WORKSPACE             | Locus.NONE       | 'list workspace'
    }

    def 'identities parse from their text form'() {
        expect:
        OperationId.parse(text) == OperationId.of(verb, subject, locus)

        where:
        text                            | verb          | subject                      | locus
        'list event in collection'      | Verb.LIST     | EntityType.EVENT             | Locus.COLLECTION
        'add collection in workspace'   | Verb.ADD      | Locus.COLLECTION             | Locus.WORKSPACE
        'import calendar in collection' | Verb.IMPORT   | SerializationFamily.CALENDAR | Locus.COLLECTION
        'generate event'                | Verb.GENERATE | EntityType.EVENT             | Locus.NONE
        '  list   workspace '           | Verb.LIST     | Locus.WORKSPACE              | Locus.NONE
    }

    def 'parsing rejects words outside the vocabulary'() {
        when:
        OperationId.parse(text)

        then:
        thrown(IllegalArgumentException)

        where:
        text << ['fetch event in collection', 'list widget in collection', 'list event in nowhere',
                 'list event at collection', 'list', 'list none', 'list channel']
    }

    def 'a non-container locus cannot be a subject'() {
        when:
        OperationId.of(Verb.LIST, subject, Locus.NONE)

        then:
        thrown(IllegalArgumentException)

        where:
        subject << [Locus.NONE, Locus.CHANNEL]
    }

    def 'parameter descriptors validate themselves'() {
        when:
        build()

        then:
        thrown(IllegalArgumentException)

        where:
        build << [
                { Param.string('Collection', 'x') },
                { Param.choice('type', 'x', []) },
                { Param.choice('type', 'x', ['a', 'b']).defaultTo('c') },
                { new Param('a', ParamType.STRING, true, 'd', 'x', false, []) },
        ]
    }

    def 'enum choices derive from an enum class'() {
        expect:
        Param.choice('type', 'x', SerializationFamily).choices() == ['calendar', 'card']
    }

    def 'operations reject duplicate parameter names and blank descriptions'() {
        when:
        Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION)
                .description(description).params(*params)
                .handler({ c, p -> new Result.Status('ok') }).build()

        then:
        thrown(IllegalArgumentException)

        where:
        description | params
        ''          | []
        'ok'        | [Param.string('a', 'x'), Param.string('a', 'y')]
    }

    def 'execute resolves defaults and required parameters before the handler runs'() {
        given:
        def op = Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION)
                .description('List events')
                .params(Param.string('collection', 'x').asRequired(),
                        Param.choice('format', 'x', ['text', 'json']).defaultTo('text'),
                        Param.bool('uids-only', 'x'))
                .handler({ ctx, p -> new Result.Status("${p.require('collection')}/${p.string('format').get()}/${p.bool('uids-only')}") })
                .build()
        def ctx = Context.builder().activeWorkspace('w').build()

        expect:
        (op.execute(ctx, Params.of(collection: 'work')) as Result.Status).message() == 'work/text/false'
        (op.execute(ctx, Params.of(collection: 'work', format: 'json', 'uids-only': 'true')) as Result.Status).message() == 'work/json/true'

        when:
        op.execute(ctx, Params.empty())

        then:
        thrown(OperationException.Usage)

        when:
        op.execute(ctx, Params.of(collection: 'work', format: 'xml'))

        then:
        thrown(OperationException.Usage)
    }

    def 'exposure defaults to every surface and can be restricted'() {
        given:
        def all = Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION).description('x')
                .handler({ c, p -> new Result.Status('ok') }).build()
        def cli = Operation.define(Verb.REMOVE, EntityType.EVENT, Locus.COLLECTION).description('x')
                .only(Surface.CLI).handler({ c, p -> new Result.Status('ok') }).build()

        expect:
        all.exposedTo(Surface.CLI) && all.exposedTo(Surface.MCP)
        cli.exposedTo(Surface.CLI) && !cli.exposedTo(Surface.MCP)
    }

    def 'typed access converts strings and lists'() {
        given:
        def p = Params.of(n: '42', bad: 'x', many: ['a', 'b'], one: 'c')

        expect:
        p.integer('n').get() == 42
        p.strings('many') == ['a', 'b']
        p.strings('one') == ['c']
        p.string('many').get() == 'a'
        !p.has('missing')
        p.string('missing', 'dflt') == 'dflt'

        when:
        p.integer('bad')

        then:
        thrown(OperationException.Usage)
    }

    def 'context refuses stores that were not configured'() {
        when:
        Context.builder().build().calendarStore()

        then:
        thrown(OperationException)
    }
}
