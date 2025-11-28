package org.ical4j.command

import net.fortuna.ical4j.validate.ValidationEntry
import net.fortuna.ical4j.validate.ValidationResult
import spock.lang.Specification

class DefaultOutputHandlersTest extends Specification {

    def 'test stdout report handler'() {
        given: 'a validation result'
        ValidationResult result = [new ValidationEntry('message', ValidationEntry.Severity.WARNING, 'context')]

        when: 'stdout report handler is called'
        StringWriter writer = []
        DefaultOutputHandlers.STDOUT_REPORT_PRINTER(writer).accept(result)

        then: 'result is rendered correctly'
        writer as String == '- WARNING: context - message\n'
    }
}
