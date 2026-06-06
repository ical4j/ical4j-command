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
package org.ical4j.command.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CollectionService {

    @Tool(description = "List all actions in the collection.")
    public String listActions(String collectionName) {
        return "List of actions";
    }

    @Tool(description = "List all availability in the collection.")
    public String listAvailability(String collectionName) {
        return "List of availability";
    }

    @Tool(description = "List all entities in the collection.")
    public String listEntities(String collectionName) {
        return "List of entities";
    }

    @Tool(description = "List all events in the collection.")
    public String listEvents(String collectionName) {
        return "List of events";
    }

    @Tool(description = "List all issues in the collection.")
    public String listIssues(String collectionName) {
        return "List of issues";
    }

    @Tool(description = "List all notes in the collection.")
    public String listNotes(String collectionName) {
        return "List of notes";
    }

    @Tool(description = "List all observances in the collection.")
    public String listObservances(String collectionName) {
        return "List of observances";
    }

    @Tool(description = "List all reports in the collection.")
    public String listReports(String collectionName) {
        return "List of reports";
    }

    @Tool(description = "List all resources in the collection.")
    public String listResources(String collectionName) {
        return "List of resources";
    }

    public String importCalendar(String name) {
        return "Imported calendar: " + name;
    }

    public String importCard(String name) {
        return "Imported card: " + name;
    }

    public String removeCalendar(String name) {
        return "Removed calendar: " + name;
    }
    public String removeCard(String name) {
        return "Removed card: " + name;
    }

    public String updateCalendar(String name) {
        return "Updated calendar: " + name;
    }

    public String updateCard(String name) {
        return "Updated card: " + name;
    }

    public String getCalendar(String name) {
        return "Info for calendar: " + name;
    }

    public String getCard(String name) {
        return "Info for card: " + name;
    }
}
