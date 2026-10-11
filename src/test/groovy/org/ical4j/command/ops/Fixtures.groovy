package org.ical4j.command.ops

import org.ical4j.connector.local.LocalCalendarStore
import org.ical4j.connector.local.LocalCardStore

import java.nio.file.Path

/** Shared fixtures for handler specs: local stores under a temp dir and sample data. */
class Fixtures {

    static Context context(Path dir, String workspace = 'work') {
        Context.builder().activeWorkspace(workspace)
                .calendarStore(new LocalCalendarStore(dir.resolve('calendars').toFile()))
                .cardStore(new LocalCardStore(dir.resolve('vcards').toFile()))
                .build()
    }

    static Operation op(String tuple) {
        Operations.standard().require(OperationId.parse(tuple))
    }

    static String event(String uid, String summary, String start = '20260910T100000Z') {
        """BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//ict test//EN
BEGIN:VEVENT
UID:$uid
DTSTAMP:20260905T000000Z
DTSTART:$start
SUMMARY:$summary
END:VEVENT
END:VCALENDAR
"""
    }

    static String todo(String uid, String summary) {
        """BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//ict test//EN
BEGIN:VTODO
UID:$uid
DTSTAMP:20260905T000000Z
SUMMARY:$summary
END:VTODO
END:VCALENDAR
"""
    }

    static String card(String uid, String name) {
        """BEGIN:VCARD
VERSION:4.0
UID:$uid
FN:$name
N:${name.split(' ').reverse().join(';')};;;
END:VCARD
"""
    }

    static Path write(Path dir, String name, String content) {
        def f = dir.resolve(name)
        f.toFile().text = content
        f
    }
}
