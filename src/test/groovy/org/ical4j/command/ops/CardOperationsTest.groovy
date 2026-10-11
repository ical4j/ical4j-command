package org.ical4j.command.ops

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static org.ical4j.command.ops.Fixtures.*

class CardOperationsTest extends Specification {

    @TempDir
    Path dir

    Context ctx

    def setup() {
        ctx = context(dir)
        op('add collection in workspace').execute(ctx, Params.of(collection: 'people', type: 'card'))
    }

    def "'import card in collection' merges a file and reports the UIDs"() {
        given:
        def file = write(dir, 'a.vcf', card('a@ict', 'Ada Lovelace'))

        when:
        def result = op('import card in collection').execute(ctx, Params.of(collection: 'people', file: file.toString()))

        then:
        (result as Result.Identifiers).ids() == ['a@ict']
        ctx.cardStore().getCollection('people', 'work').listObjectUIDs() == ['a@ict']

        when:
        op('import card in collection').execute(ctx, Params.of(collection: 'people', file: write(dir, 'bad.vcf', 'nope').toString()))

        then:
        thrown(OperationException.InvalidData)
    }

    def "'export card in collection' returns one card by UID or the whole collection"() {
        given:
        op('import card in collection').execute(ctx, Params.of(collection: 'people', file: write(dir, 'a.vcf', card('a@ict', 'Ada Lovelace')).toString()))

        when:
        def one = op('export card in collection').execute(ctx, Params.of(collection: 'people', uid: 'a@ict')) as Result.CardData

        then:
        one.card().getUid().value == 'a@ict'

        when:
        def all = op('export card in collection').execute(ctx, Params.of(collection: 'people')) as Result.CardData

        then:
        all.card() != null

        when:
        op('export card in collection').execute(ctx, Params.of(collection: 'people', uid: 'nope'))

        then:
        thrown(OperationException.NotFound)
    }

    def "'remove card in collection' removes one card by UID"() {
        given:
        op('import card in collection').execute(ctx, Params.of(collection: 'people', file: write(dir, 'a.vcf', card('a@ict', 'Ada Lovelace')).toString()))

        when:
        def result = op('remove card in collection').execute(ctx, Params.of(collection: 'people', uid: 'a@ict'))

        then:
        (result as Result.Status).message() == "Removed card 'a@ict'"
        ctx.cardStore().getCollection('people', 'work').listObjectUIDs() == []

        when:
        op('remove card in collection').execute(ctx, Params.of(collection: 'people', uid: 'a@ict'))

        then:
        thrown(OperationException.NotFound)
    }
}
