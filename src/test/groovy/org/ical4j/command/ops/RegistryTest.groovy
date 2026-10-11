package org.ical4j.command.ops

import org.ical4j.command.vocab.EntityType
import org.ical4j.command.vocab.Locus
import org.ical4j.command.vocab.Verb
import spock.lang.Specification

class RegistryTest extends Specification {

    static Operation op(Verb verb, EntityType type, Locus locus, Surface... only) {
        def b = Operation.define(verb, type, locus).description("$verb $type")
                .handler({ c, p -> new Result.Status("$verb $type") })
        if (only) b.only(only)
        b.build()
    }

    def 'registering the same tuple twice fails naming the tuple'() {
        given:
        def builder = Registry.builder().add(op(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))

        when:
        builder.add(op(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('list event in collection')
    }

    def 'lookups honour registration order and surface exposure'() {
        given:
        def registry = Registry.builder()
                .add(op(Verb.REMOVE, EntityType.EVENT, Locus.COLLECTION, Surface.CLI))
                .add(op(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))
                .add(op(Verb.LIST, EntityType.NOTE, Locus.COLLECTION))
                .build()

        expect:
        registry.size() == 3
        registry.all()*.id()*.toString() == ['remove event in collection', 'list event in collection', 'list note in collection']
        registry.forSurface(Surface.MCP)*.id()*.toString() == ['list event in collection', 'list note in collection']
        registry.byVerb(Surface.CLI).keySet()*.name() == ['LIST', 'REMOVE']
        registry.byVerb(Surface.CLI)[Verb.LIST]*.id()*.subjectKey() == ['event', 'note']
        registry.byVerb(Surface.MCP).keySet()*.name() == ['LIST']
        registry.find(OperationId.of(Verb.LIST, EntityType.EVENT, Locus.COLLECTION)).present
        !registry.find(OperationId.of(Verb.GET, EntityType.EVENT, Locus.COLLECTION)).present
    }

    def 'execute by identity resolves parameters and reports unknown tuples'() {
        given:
        def registry = Registry.builder().add(op(Verb.LIST, EntityType.EVENT, Locus.COLLECTION)).build()
        def ctx = Context.builder().build()

        expect:
        (registry.execute(OperationId.of(Verb.LIST, EntityType.EVENT, Locus.COLLECTION), ctx, Params.empty()) as Result.Status).message() == 'LIST EVENT'

        when:
        registry.execute(OperationId.of(Verb.GET, EntityType.EVENT, Locus.COLLECTION), ctx, Params.empty())

        then:
        thrown(OperationException.NotFound)
    }

    def 'an empty registry has nothing for any surface'() {
        expect:
        Registry.empty().forSurface(Surface.CLI).empty
        Registry.empty().byVerb(Surface.MCP).isEmpty()
    }
}
