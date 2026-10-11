package org.ical4j.command.cli

import org.ical4j.command.Version
import org.ical4j.command.config.CommandConfig
import org.ical4j.command.ops.Operation
import org.ical4j.command.ops.OperationException
import org.ical4j.command.ops.Param
import org.ical4j.command.ops.Params
import org.ical4j.command.ops.Registry
import org.ical4j.command.ops.Result
import org.ical4j.command.ops.Surface
import org.ical4j.command.vocab.EntityType
import org.ical4j.command.vocab.Locus
import org.ical4j.command.vocab.SerializationFamily
import org.ical4j.command.vocab.Verb
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * The CLI view built from a stub registry: tree shape, option derivation, exit codes and the
 * stdout/stderr split. Handlers here are stubs; real operations are covered by MainTest.
 */
class CommandTreeTest extends Specification {

    @TempDir
    Path dir

    ByteArrayOutputStream out = new ByteArrayOutputStream()
    ByteArrayOutputStream err = new ByteArrayOutputStream()
    Params received

    def 'root help lists one command per verb with CLI operations, plus config'() {
        when:
        def code = run(registry(), '--help')

        then:
        code == 0
        commands(out.toString()) == ['list', 'export', 'config', 'mcp']
        err.toString().isEmpty()
    }

    def 'verb help lists the types under that verb'() {
        when:
        def code = run(registry(), 'list', '--help')

        then:
        code == 0
        commands(out.toString()) == ['event', 'collection']
    }

    def 'operations not exposed to the CLI have no command'() {
        given:
        def reg = Registry.builder()
                .add(stub(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))
                .add(Operation.define(Verb.REMOVE, EntityType.EVENT, Locus.COLLECTION).description('mcp only')
                        .only(Surface.MCP).handler({ c, p -> new Result.Status('x') }).build())
                .build()

        when:
        run(reg, '--help')

        then:
        commands(out.toString()) == ['list', 'config', 'mcp']
    }

    def 'options are derived from parameter descriptors'() {
        given:
        def op = Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION).description('d')
                .params(Param.string('collection', 'Collection name').asRequired(),
                        Param.integer('limit', 'At most this many'),
                        Param.bool('all', 'Everything'),
                        Param.choice('kind', 'Kind of thing', ['a', 'b']).defaultTo('a'),
                        Param.path('file', 'Input file').asMultiple())
                .handler({ c, p -> received = p; new Result.Status('ok') }).build()
        def cli = Main.commandLine(Registry.builder().add(op).build(), out, err,
                InputStream.nullInputStream(), { new CommandConfig(dir.toFile()) })
        def leaf = cli.subcommands.list.subcommands.event.commandSpec

        expect:
        with(leaf.findOption('--collection')) {
            required()
            type() == String
            description()[0] == 'Collection name'
        }
        !leaf.findOption('--limit').required()
        leaf.findOption('--all').type() == boolean
        leaf.findOption('--kind').completionCandidates().toList() == ['a', 'b']
        leaf.findOption('--kind').description()[0] == 'Kind of thing (one of: a, b) (default: a)'
        leaf.findOption('--file').type() == String[]
        leaf.positionalParameters().isEmpty()

        when:
        def code = cli.execute('list', 'event', '--collection', 'work', '--all', '--file', 'x.ics', '--file', 'y.ics')

        then: 'only matched options reach the handler, and descriptor defaults are applied'
        code == 0
        received.asMap() == [collection: 'work', all: true, file: ['x.ics', 'y.ics'], kind: 'a']
        out.toString() == 'ok\n'
    }

    def 'two operations with the same verb and subject cannot both be on the CLI'() {
        given:
        def reg = Registry.builder()
                .add(stub(Verb.LIST, Locus.COLLECTION, Locus.WORKSPACE))
                .add(stub(Verb.LIST, Locus.COLLECTION, Locus.NONE))
                .build()

        when:
        run(reg, '--help')

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("'list collection in workspace'")
        e.message.contains("'list collection'")
    }

    def 'a parameter may not shadow a global option'() {
        given:
        def reg = Registry.builder().add(Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION)
                .description('d').params(Param.string('format', 'x'))
                .handler({ c, p -> new Result.Status('x') }).build()).build()

        when:
        run(reg, '--help')

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('--format')
    }

    def 'exit codes: #scenario'() {
        when:
        def code = run(registry(), *args)

        then:
        code == expected
        (out.toString().isEmpty()) == (expected != 0)
        (err.toString().isEmpty()) == (expected == 0)
        err.toString().contains(diagnostic)

        where:
        scenario                 | args                                                 | expected | diagnostic
        'success'                | ['list', 'event', '--collection', 'work']            | 0        | ''
        'not found'              | ['list', 'event', '--collection', 'missing']         | 1        | 'ict: No such collection: missing'
        'store failure'          | ['list', 'event', '--collection', 'broken']          | 1        | 'ict: disk on fire'
        'missing type'           | ['list']                                             | 2        | 'Usage: ict list'
        'no command'             | []                                                   | 2        | 'Usage: ict'
        'unknown type'           | ['list', 'widget']                                   | 2        | "Unmatched argument at index 1: 'widget'"
        'missing required'       | ['list', 'event']                                    | 2        | "Missing required option: '--collection=<collection>'"
        'usage from handler'     | ['list', 'event', '--collection', 'usage']           | 2        | "Try 'ict list event --help'"
        'xml for a list'         | ['list', 'event', '--collection', 'work', '--format', 'xml'] | 2 | 'XML output is only available'
        'unexpected exception'   | ['list', 'event', '--collection', 'bug']             | 1        | 'java.lang.IllegalArgumentException: bad'
    }

    def 'global options apply on either side of the verb'() {
        when:
        run(registry(), *args)

        then:
        out.toString().trim() == '[ "e1@ict" ]'

        where:
        args << [['--format', 'json', 'list', 'event', '--collection', 'work'],
                 ['list', 'event', '--collection', 'work', '--format', 'JSON']]
    }

    def 'workspace option overrides the configured workspace'() {
        given:
        def reg = Registry.builder().add(Operation.define(Verb.LIST, Locus.COLLECTION, Locus.WORKSPACE)
                .description('d').handler({ c, p -> new Result.Status(c.activeWorkspace()) }).build()).build()

        expect:
        run(reg, 'list', 'collection') == 0
        out.toString() == 'default\n'

        when:
        out.reset()
        run(reg, 'list', 'collection', '--workspace', 'other')

        then:
        out.toString() == 'other\n'
    }

    def 'version prints the embedded build version to stdout'() {
        when:
        def code = run(registry(), '--version')

        then:
        code == 0
        out.toString().startsWith("ict ${Version.get()} ")
        Version.get() != 'unknown'
    }

    // ---- fixtures ---------------------------------------------------------------------------

    private int run(Registry registry, String... args) {
        Main.commandLine(registry, out, err, InputStream.nullInputStream(),
                { new CommandConfig(dir.toFile()) }).execute(args)
    }

    private static Registry registry() {
        Registry.builder()
                .add(Operation.define(Verb.LIST, EntityType.EVENT, Locus.COLLECTION).description('List events')
                        .params(Param.string('collection', 'Collection name').asRequired())
                        .handler({ c, p ->
                            switch (p.require('collection')) {
                                case 'missing': throw new OperationException.NotFound('No such collection: missing')
                                case 'broken': throw new OperationException.Store('disk on fire', null)
                                case 'usage': throw new OperationException.Usage('bad usage')
                                case 'bug': throw new IllegalArgumentException('bad')
                                default: return new Result.Identifiers(['e1@ict'])
                            }
                        }).build())
                .add(stub(Verb.LIST, Locus.COLLECTION, Locus.WORKSPACE))
                .add(stub(Verb.EXPORT, SerializationFamily.CALENDAR, Locus.COLLECTION))
                .build()
    }

    private static Operation stub(Verb verb, def subject, Locus locus) {
        Operation.define(verb, subject, locus).description("stub ${verb} ${subject}")
                .handler({ c, p -> new Result.Status('ok') }).build()
    }

    /** Command names from the commands/types section of usage help. */
    private static List<String> commands(String help) {
        def lines = help.readLines()
        def start = lines.findIndexOf { it ==~ /(Commands|Types):/ }
        lines.drop(start + 1).takeWhile { it.startsWith('  ') }.collect { it.trim().split(/\s+/)[0] }
    }
}
