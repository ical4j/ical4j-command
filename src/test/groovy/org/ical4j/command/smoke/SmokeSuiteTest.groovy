package org.ical4j.command.smoke

import org.ical4j.command.ops.Operations
import spock.lang.Specification

import java.nio.file.Path

/**
 * The native smoke suite must invoke every registered operation (design D8). The suite enforces
 * this itself when it runs; checking here as well makes `./gradlew test` fail first, without a
 * native toolchain.
 */
class SmokeSuiteTest extends Specification {

    def 'every operation in the standard registry has a smoke step'() {
        expect:
        SmokeSuite.missing() == [] as Set
    }

    def 'every step that claims an operation names a real one'() {
        given:
        def registered = Operations.standard().all()*.id()*.toString() as Set

        expect:
        SmokeSuite.steps(Path.of('fixtures'))*.covers().findAll { it != null && !registered.contains(it) } == []
    }
}
