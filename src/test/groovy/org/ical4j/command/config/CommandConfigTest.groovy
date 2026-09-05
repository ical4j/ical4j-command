package org.ical4j.command.config

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class CommandConfigTest extends Specification {

    @TempDir
    Path dataDir

    def 'defaults come from the bundled resource and the file is created under the data directory'() {
        when:
        def config = new CommandConfig(dataDir.toFile())

        then:
        config.activeWorkspace == 'default'
        config.userAgent == 'ict/1.0'
        config.configFile() == dataDir.resolve('iCal4j/ictrc.toml').toFile()
        config.configFile().exists()
        config.configFile().text.contains('[workspace]')
    }

    def 'settings persist to the file and survive a reload'() {
        given:
        def config = new CommandConfig(dataDir.toFile())

        when:
        def previous = config.setActiveWorkspace('work')

        then:
        previous == 'default'
        config.activeWorkspace == 'work'
        new CommandConfig(dataDir.toFile()).activeWorkspace == 'work'
    }

    def 'store directories sit under the application directory and are configurable'() {
        given:
        def config = new CommandConfig(dataDir.toFile())

        expect:
        config.calendarStoreDirectory() == dataDir.resolve('iCal4j/calendars').toFile()
        config.cardStoreDirectory() == dataDir.resolve('iCal4j/vcards').toFile()

        when:
        config.setCalendarStorePath('cal')

        then:
        config.calendarStoreDirectory() == dataDir.resolve('iCal4j/cal').toFile()
    }

    def 'the context carries the active workspace and opens local stores lazily'() {
        given:
        def config = new CommandConfig(dataDir.toFile())
        config.setActiveWorkspace('work')

        when:
        def context = config.context()

        then:
        context.activeWorkspace() == 'work'
        !dataDir.resolve('iCal4j/calendars').toFile().exists()

        when:
        context.calendarStore().addCollection('events', 'work')

        then:
        dataDir.resolve('iCal4j/calendars/work/events').toFile().directory

        when:
        context.cardStore().addCollection('people', 'work')

        then:
        dataDir.resolve('iCal4j/vcards/work/people').toFile().directory
    }
}
