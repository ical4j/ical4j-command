package org.ical4j.command.cli

import com.fasterxml.jackson.databind.ObjectMapper
import org.ical4j.command.config.CommandConfig
import org.ical4j.command.ops.OperationId
import org.ical4j.command.ops.Operations
import org.ical4j.command.ops.Surface
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import static org.ical4j.command.ops.Fixtures.card
import static org.ical4j.command.ops.Fixtures.event
import static org.ical4j.command.ops.Fixtures.todo
import static org.ical4j.command.ops.Fixtures.write

/**
 * Runs {@link Main} in-process, as a user would, against a temp-dir store: one scenario per
 * CLI-exposed operation in the standard registry, in an order that builds up state.
 */
@Stepwise
class MainTest extends Specification {

    @Shared
    @TempDir
    Path dir

    @Shared
    Set<String> exercised = []

    def "'add collection in workspace': ict add collection"() {
        expect:
        ict('add', 'collection', '--collection', 'work') == ok("Added calendar collection 'work'\n")
        ict('add', 'collection', '--collection', 'people', '--type', 'card', '--description', 'Contacts') ==
                ok("Added card collection 'people'\n")
        ict('add', 'collection', '--collection', 'work').code == 1

        cleanup:
        exercised << 'add collection in workspace'
    }

    def "'list collection in workspace': ict list collection"() {
        when:
        def text = ict('list', 'collection')
        def json = ict('list', 'collection', '--format', 'json')

        then:
        text.code == 0
        text.out.readLines()[0].split(/\s+/) as List == ['name', 'type', 'description', 'objects']
        text.out.readLines()*.split(/\s+/)*.getAt(0).drop(1) == ['work', 'people']
        json.code == 0
        new ObjectMapper().readValue(json.out, List)*.name == ['work', 'people']

        cleanup:
        exercised << 'list collection in workspace'
    }

    def "'get collection in workspace': ict get collection"() {
        expect:
        ict('get', 'collection', '--collection', 'people', '--type', 'card').out ==
                'name: people\ntype: card\ndescription: Contacts\nobjects: 0\n'
        ict('get', 'collection', '--collection', 'missing') == fail(1, 'ict: No such collection: missing\n')

        cleanup:
        exercised << 'get collection in workspace'
    }

    def "'import calendar in collection': ict import calendar from a file and from stdin"() {
        given:
        def file = write(dir, 'e1.ics', event('e1@ict', 'Planning'))

        expect:
        ict('import', 'calendar', '--collection', 'work', '--file', file.toString()) == ok('e1@ict\n')
        piped(todo('t1@ict', 'Write tests'), 'import', 'calendar', '--collection', 'work') == ok('t1@ict\n')
        ict('import', 'calendar', '--collection', 'work', '--file', dir.resolve('none.ics').toString()).code == 1

        cleanup:
        exercised << 'import calendar in collection'
    }

    def "'list event in collection', 'list action in collection', 'list note in collection': ict list <type>"() {
        expect:
        ict('list', 'event', '--collection', 'work').out.readLines()[1].startsWith('e1@ict  Planning')
        ict('list', 'action', '--collection', 'work').out.readLines()[1].startsWith('t1@ict  Write tests')
        ict('list', 'note', '--collection', 'work') == ok('')
        ict('list', 'event', '--collection', 'missing').code == 1

        cleanup:
        exercised.addAll(['list event in collection', 'list action in collection', 'list note in collection'])
    }

    def "'export calendar in collection': ict export calendar"() {
        when:
        def one = ict('export', 'calendar', '--collection', 'work', '--uid', 'e1@ict')
        def all = ict('export', 'calendar', '--collection', 'work')
        def json = ict('export', 'calendar', '--collection', 'work', '--uid', 'e1@ict', '--format', 'json')

        then:
        one.code == 0
        one.out.startsWith('BEGIN:VCALENDAR')
        one.out.contains('SUMMARY:Planning')
        all.out.contains('UID:e1@ict') && all.out.contains('UID:t1@ict')
        json.out.startsWith('[ "vcalendar"')
        ict('export', 'calendar', '--collection', 'work', '--uid', 'nope').code == 1

        cleanup:
        exercised << 'export calendar in collection'
    }

    def "'remove calendar in collection': ict remove calendar"() {
        expect:
        ict('remove', 'calendar', '--collection', 'work', '--uid', 't1@ict') == ok("Removed calendar 't1@ict'\n")
        ict('remove', 'calendar', '--collection', 'work', '--uid', 't1@ict') == fail(1, "ict: No such calendar: t1@ict\n")
        ict('remove', 'calendar', '--collection', 'work').code == 2

        cleanup:
        exercised << 'remove calendar in collection'
    }

    def "'import card in collection', 'list entity in collection', 'export card in collection', 'remove card in collection'"() {
        given:
        def file = write(dir, 'ada.vcf', card('c1@ict', 'Ada Lovelace'))

        expect:
        ict('import', 'card', '--collection', 'people', '--file', file.toString()) == ok('c1@ict\n')
        ict('list', 'entity', '--collection', 'people').out == 'uid     name\nc1@ict  Ada Lovelace\n'
        ict('export', 'card', '--collection', 'people', '--uid', 'c1@ict').out.startsWith('BEGIN:VCARD')
        ict('remove', 'card', '--collection', 'people', '--uid', 'c1@ict') == ok("Removed card 'c1@ict'\n")
        ict('list', 'entity', '--collection', 'people') == ok('')

        cleanup:
        exercised.addAll(['import card in collection', 'list entity in collection', 'export card in collection',
                          'remove card in collection'])
    }

    def "'remove collection in workspace': ict remove collection"() {
        expect:
        ict('remove', 'collection', '--collection', 'people', '--type', 'card') == ok("Removed card collection 'people'\n")
        ict('list', 'collection').out.readLines()*.split(/\s+/)*.getAt(0).drop(1) == ['work']

        cleanup:
        exercised << 'remove collection in workspace'
    }

    def 'ict config reads and writes the configuration file'() {
        expect:
        ict('config', 'workspace.active') == ok('default\n')
        ict('config', 'user.name', 'Ada', 'Lovelace') == ok("Set 'user.name' to 'Ada Lovelace'\n")
        ict('config', 'user.name') == ok('Ada Lovelace\n')
        ict('config').out.readLines().contains('user.name = Ada Lovelace')
        ict('config', 'nonsense.key', 'x').code == 1
        ict('config', 'user.nobody').code == 1
    }

    def 'every CLI operation in the standard registry was exercised above'() {
        expect:
        Operations.standard().forSurface(Surface.CLI)*.id()*.toString().toSet() - exercised == [] as Set
        exercised.every { OperationId.parse(it) }
    }

    // ---- fixtures ---------------------------------------------------------------------------

    private Run ict(String... args) {
        piped(null, args)
    }

    private Run piped(String stdin, String... args) {
        def out = new ByteArrayOutputStream()
        def err = new ByteArrayOutputStream()
        def input = new ByteArrayInputStream((stdin ?: '').getBytes(StandardCharsets.UTF_8))
        int code = Main.run(args, out, err, input, { new CommandConfig(dir.toFile()) })
        new Run(code, out.toString(StandardCharsets.UTF_8).normalize(), err.toString(StandardCharsets.UTF_8))
    }

    private static Run ok(String out) {
        new Run(0, out, '')
    }

    private static Run fail(int code, String err) {
        new Run(code, '', err)
    }

    @groovy.transform.Canonical
    static class Run {
        int code
        String out
        String err
    }
}
