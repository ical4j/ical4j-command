package org.ical4j.command

import org.ical4j.command.config.CommandConfig
import org.ical4j.connector.CalendarCollection
import org.ical4j.connector.CardCollection
import org.ical4j.connector.ObjectStore
import spock.lang.Specification

/*
 * Copyright (c) 2025, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

class ListCommandTest extends Specification {

    def 'test list command lists collection names from the active workspace'() {
        given: 'mock calendar and card stores'
        ObjectStore calendarStore = Mock()
        ObjectStore cardStore = Mock()
        CalendarCollection calendarCollection = Mock()
        CardCollection cardCollection = Mock()

        calendarStore.getCollections('default') >> [calendarCollection]
        cardStore.getCollections('default') >> [cardCollection]
        calendarCollection.getDisplayName() >> 'My Calendar'
        cardCollection.getDisplayName() >> 'My Contacts'

        and: 'a config exposing those stores for the active workspace'
        CommandConfig config = new CommandConfig(null) {
            @Override String getActiveWorkspace() { 'default' }
            @Override ObjectStore getCalendarStore() { calendarStore }
            @Override ObjectStore getCardStore() { cardStore }
        }

        and: 'a list command capturing its output'
        def output
        def command = new ListCommand((o) -> output = o, config)

        when: 'the command is executed'
        def result = command.call()

        then: 'it succeeds and reports both collection names'
        result == 0
        output as Set == ['My Calendar', 'My Contacts'] as Set
    }
}
