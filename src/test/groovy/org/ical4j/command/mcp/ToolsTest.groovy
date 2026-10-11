package org.ical4j.command.mcp

import org.ical4j.command.ops.Context
import org.ical4j.command.ops.Operation
import org.ical4j.command.ops.OperationException
import org.ical4j.command.ops.Param
import org.ical4j.command.ops.Registry
import org.ical4j.command.ops.Result
import org.ical4j.command.ops.Surface
import org.ical4j.command.vocab.EntityType
import org.ical4j.command.vocab.Locus
import org.ical4j.command.vocab.SerializationFamily
import org.ical4j.command.vocab.Verb
import spock.lang.Specification

/**
 * Tool derivation from stub operations. The protocol round trip is covered by McpServerTest.
 */
class ToolsTest extends Specification {

    def 'tools are named <verb>_<subject> and only MCP-exposed operations become tools'() {
        given:
        def registry = Registry.builder()
                .add(stub(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))
                .add(stub(Verb.IMPORT, SerializationFamily.CALENDAR, Locus.COLLECTION))
                .add(Operation.define(Verb.REMOVE, EntityType.EVENT, Locus.COLLECTION).description('cli only')
                        .only(Surface.CLI).handler({ c, p -> new Result.Status('x') }).build())
                .build()

        expect:
        Tools.specifications(registry, { context() })*.tool()*.name() == ['list_event', 'import_calendar']
    }

    def 'the input schema is derived from the parameter descriptors'() {
        given:
        def op = Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION).description('List events')
                .params(Param.string('collection', 'Collection name').asRequired(),
                        Param.string('uid', 'One UID'),
                        Param.integer('limit', 'At most'),
                        Param.bool('all', 'Everything'),
                        Param.choice('kind', 'Kind', ['a', 'b']).defaultTo('b'),
                        Param.path('file', 'Files').asMultiple())
                .handler({ c, p -> new Result.Status('x') }).build()

        when:
        def tool = Tools.tool(op)

        then:
        tool.name() == 'list_event'
        tool.description() == 'List events'
        tool.annotations().readOnlyHint()
        !tool.annotations().destructiveHint()
        tool.inputSchema() == [
                type                : 'object',
                properties          : [
                        collection: [type: 'string', description: 'Collection name'],
                        uid       : [type: 'string', description: 'One UID'],
                        limit     : [type: 'integer', description: 'At most'],
                        all       : [type: 'boolean', description: 'Everything', default: false],
                        kind      : [type: 'string', enum: ['a', 'b'], description: 'Kind', default: 'b'],
                        file      : [type: 'array', items: [type: 'string'], description: 'Files']],
                required            : ['collection'],
                additionalProperties: false]
    }

    def 'destructive verbs are annotated as such'() {
        expect:
        Tools.tool(stub(Verb.REMOVE, SerializationFamily.CALENDAR, Locus.COLLECTION)).annotations().destructiveHint()
    }

    def 'a call runs the handler with the arguments; failures become isError results'() {
        given:
        def registry = Registry.builder().add(Operation.define(Verb.GET, Locus.COLLECTION, Locus.WORKSPACE)
                .description('Get').params(Param.string('collection', 'Name').asRequired())
                .handler({ c, p ->
                    switch (p.require('collection')) {
                        case 'missing': throw new OperationException.NotFound('No such collection: missing')
                        case 'bug': throw new IllegalStateException('boom')
                        case 'ics': return new Result.CalendarData(new net.fortuna.ical4j.model.Calendar())
                        default: return new Result.Descriptor([name: p.require('collection'), ws: c.activeWorkspace()])
                    }
                }).build()).build()
        def handler = Tools.specifications(registry, { context() })[0].callHandler()

        expect:
        call(handler, [collection: 'work']).with { !isError() && text(it) == '{"name":"work","ws":"default"}\n' }
        call(handler, [collection: 'ics']).with { !isError() && text(it).startsWith('BEGIN:VCALENDAR') }
        call(handler, [collection: 'missing']).with { isError() && text(it) == 'No such collection: missing' }
        call(handler, [collection: 'bug']).with { isError() && text(it) == 'boom' }
        call(handler, null).with { isError() && text(it) == 'Missing required parameter: collection' }
    }

    def 'two exposed operations with the same tool name are rejected'() {
        given:
        def registry = Registry.builder()
                .add(stub(Verb.LIST, Locus.COLLECTION, Locus.WORKSPACE))
                .add(stub(Verb.LIST, Locus.COLLECTION, Locus.NONE))
                .build()

        when:
        Tools.specifications(registry, { context() })

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('list_collection')
    }

    private static def call(handler, Map args) {
        handler.apply(null, new io.modelcontextprotocol.spec.McpSchema.CallToolRequest('t', args, null))
    }

    private static String text(result) {
        result.content()[0].text()
    }

    private static Context context() {
        Context.builder().build()
    }

    private static Operation stub(Verb verb, def subject, Locus locus) {
        Operation.define(verb, subject, locus).description('stub').handler({ c, p -> new Result.Status('ok') }).build()
    }
}
