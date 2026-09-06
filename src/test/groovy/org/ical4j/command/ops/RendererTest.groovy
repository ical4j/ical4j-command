package org.ical4j.command.ops

import com.fasterxml.jackson.databind.ObjectMapper
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.vcard.VCardBuilder
import spock.lang.Specification

import static org.ical4j.command.ops.Fixtures.card
import static org.ical4j.command.ops.Fixtures.event

class RendererTest extends Specification {

    def renderer = new Renderer()

    static Object parse(String json) { new ObjectMapper().readValue(json, Object) }

    def 'status, identifiers and descriptors render as text lines and JSON values'() {
        expect:
        renderer.render(result, Format.TEXT) == text
        parse(renderer.render(result, Format.JSON)) == json

        where:
        result                                                                    | text                              | json
        new Result.Status('Done')                                                 | 'Done\n'                          | [status: 'Done']
        new Result.Identifiers(['a', 'b'])                                        | 'a\nb\n'                          | ['a', 'b']
        new Result.Identifiers([])                                                | ''                                | []
        new Result.Descriptor([name: 'events', type: 'calendar'])                 | 'name: events\ntype: calendar\n'  | [name: 'events', type: 'calendar']
        new Result.Descriptors([])                                                | ''                                | []
    }

    def 'descriptor lists render as an aligned table over the union of columns'() {
        given:
        def result = new Result.Descriptors([
                new Result.Descriptor([uid: 'e1', summary: 'Standup', start: '20260910T090000Z']),
                new Result.Descriptor([uid: 'e22', summary: 'A much longer summary']),
        ])

        expect:
        renderer.render(result, Format.TEXT) == '''\
uid  summary                start
e1   Standup                20260910T090000Z
e22  A much longer summary  
'''
        parse(renderer.render(result, Format.JSON)) == [
                [uid: 'e1', summary: 'Standup', start: '20260910T090000Z'],
                [uid: 'e22', summary: 'A much longer summary']]
    }

    def 'calendar data renders as iCalendar, jCal and xCal'() {
        given:
        def result = new Result.CalendarData(new CalendarBuilder().build(new StringReader(event('a@ict', 'First'))))

        when:
        def text = renderer.render(result, Format.TEXT)
        def json = parse(renderer.render(result, Format.JSON))
        def xml = renderer.render(result, Format.XML)

        then:
        text.startsWith('BEGIN:VCALENDAR')
        text.contains('UID:a@ict')
        json[0] == 'vcalendar'
        json[2][0][0] == 'vevent'
        xml.startsWith('<icalendar xmlns="urn:ietf:params:xml:ns:icalendar-2.0">')
        xml.contains('<uid><parameters/><text>a@ict</text></uid>')
    }

    def 'card data renders as vCard, jCard and xCard'() {
        given:
        def result = new Result.CardData(new VCardBuilder(new StringReader(card('c1@ict', 'Ada Lovelace'))).build())

        when:
        def text = renderer.render(result, Format.TEXT)
        def json = parse(renderer.render(result, Format.JSON))
        def xml = renderer.render(result, Format.XML)

        then:
        text.startsWith('BEGIN:VCARD')
        text.contains('FN:Ada Lovelace')
        json[0] == 'vcard'
        json[1].find { it[0] == 'fn' }[3] == 'Ada Lovelace'
        xml.contains('vcard-4.0')
        xml.contains('Ada Lovelace')
    }

    def 'validation renders the report format or a clean bill'() {
        given:
        def clean = new Result.Validation([])
        def dirty = new Result.Validation([new Result.ValidationEntry('WARNING', 'context', 'message')])

        expect:
        renderer.render(clean, Format.TEXT) == 'No errors.\n'
        renderer.render(dirty, Format.TEXT) == '- WARNING: context - message\n'
        parse(renderer.render(clean, Format.JSON)) == [valid: true, entries: []]
        parse(renderer.render(dirty, Format.JSON)) == [valid: false, entries: [[severity: 'WARNING', context: 'context', message: 'message']]]
    }

    def 'XML is refused for results that have no XML form'() {
        when:
        renderer.render(new Result.Status('x'), Format.XML)

        then:
        thrown(OperationException.Usage)
    }

    def 'pretty printing indents JSON'() {
        expect:
        new Renderer(true).render(new Result.Descriptor([a: '1']), Format.JSON).contains('\n  "a"')
    }
}
