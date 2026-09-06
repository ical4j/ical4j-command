package org.ical4j.command.ops

import org.ical4j.command.vocab.EntityType
import org.ical4j.command.vocab.Locus
import org.ical4j.command.vocab.Verb
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * The admission bar (design D8): an operation may sit in the standard registry only if a
 * Spock specification names its tuple, as the literal text of {@link OperationId#toString()},
 * for example {@code 'add collection in workspace'}. Specs get the operation with
 * {@code Operations.standard().require(OperationId.parse('add collection in workspace'))}, so
 * the literal is the natural way to refer to it.
 */
class RegistryConformanceTest extends Specification {

    static final Path TEST_SOURCES = Path.of('src/test/groovy')

    def 'every operation in the standard registry is named by a specification'() {
        given:
        def registry = Operations.standard()

        expect:
        missing(registry, TEST_SOURCES, getClass().simpleName) == []
    }

    def 'every operation in the standard registry is exposed to at least one surface'() {
        expect:
        Operations.standard().all().findAll { it.exposure().empty }*.id()*.toString() == []
    }

    def 'the scanner reports unreferenced tuples and accepts referenced ones'(@TempDir Path sources) {
        given:
        def registry = Registry.builder()
                .add(op(Verb.LIST, EntityType.EVENT, Locus.COLLECTION))
                .add(op(Verb.ADD, Locus.COLLECTION, Locus.WORKSPACE))
                .build()
        def spec = sources.resolve('org/x/ListEventsTest.groovy')
        Files.createDirectories(spec.parent)
        Files.writeString(spec, "def op = Operations.standard().require(OperationId.parse('list event in collection'))")
        def self = sources.resolve('org/x/RegistryConformanceTest.groovy')
        Files.writeString(self, "'add collection in workspace' // the conformance test itself does not count")

        expect:
        missing(registry, sources, 'RegistryConformanceTest') == ['add collection in workspace']
    }

    /** Tuples in the registry that no spec under {@code sources} (other than the excluded one) mentions. */
    static List<String> missing(Registry registry, Path sources, String excludedSimpleName) {
        def texts = []
        if (Files.isDirectory(sources)) {
            Files.walk(sources).withCloseable { stream ->
                stream.filter { it.toString().endsWith('.groovy') && it.fileName.toString() != "${excludedSimpleName}.groovy" }
                        .forEach { texts << Files.readString(it) }
            }
        }
        registry.all()*.id()*.toString().findAll { tuple -> !texts.any { it.contains("'$tuple'") || it.contains("\"$tuple\"") } }
    }

    private static Operation op(Verb verb, def subject, Locus locus) {
        Operation.define(verb, subject, locus).description('x').handler({ c, p -> new Result.Status('ok') }).build()
    }
}
