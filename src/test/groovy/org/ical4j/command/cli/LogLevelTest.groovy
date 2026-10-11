package org.ical4j.command.cli

import spock.lang.Specification

class LogLevelTest extends Specification {

    def 'verbose flags map to levels'() {
        expect:
        LogLevel.fromVerbosity(LogLevel.countVerbose(args as String[])) == level

        where:
        args                          | level
        []                            | null
        ['list']                      | null
        ['-v', 'list']                | 'info'
        ['--verbose', 'list']         | 'info'
        ['-vv']                       | 'debug'
        ['-v', '-v']                  | 'debug'
        ['-vvv']                      | 'trace'
        ['--', '-v']                  | null
        ['-verbose']                  | null
        ['-version']                  | null
    }

    def 'quiet selects error unless verbosity or the environment says otherwise'() {
        given:
        def previous = System.getProperty('org.slf4j.simpleLogger.defaultLogLevel')
        System.clearProperty('org.slf4j.simpleLogger.defaultLogLevel')

        when:
        LogLevel.configure(args as String[], env)

        then:
        System.getProperty('org.slf4j.simpleLogger.defaultLogLevel') == level

        cleanup:
        if (previous == null) System.clearProperty('org.slf4j.simpleLogger.defaultLogLevel')
        else System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', previous)

        where:
        args                    | env    | level
        ['-q', 'list']          | null   | 'error'
        ['--quiet']             | null   | 'error'
        ['-q', '-v']            | null   | 'info'
        ['-q']                  | 'warn' | 'warn'
        ['--', '-q']            | null   | null
    }

    def 'environment overrides flags'() {
        given:
        def previous = System.getProperty('org.slf4j.simpleLogger.defaultLogLevel')

        when:
        LogLevel.configure(['-vv'] as String[], 'ERROR')

        then:
        System.getProperty('org.slf4j.simpleLogger.defaultLogLevel') == 'error'

        cleanup:
        if (previous == null) System.clearProperty('org.slf4j.simpleLogger.defaultLogLevel')
        else System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', previous)
    }
}
