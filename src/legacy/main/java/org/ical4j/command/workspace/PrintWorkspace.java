package org.ical4j.command.workspace;

import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.config.CommandConfig;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectCollection;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

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
@CommandLine.Command(name = "print-workspace", aliases = {"pws"}, description = "Show information about the selected workspace")
public class PrintWorkspace extends AbstractCommand<List<String>> {

    @CommandLine.Parameters(index = "0", description = "Workspace ID", arity = "0..1")
    private List<String> workspaceId;

    public PrintWorkspace() {
        super(DefaultOutputHandlers.STDOUT_LIST_PRINTER());
    }

    @Override
    public Integer call() throws Exception {
        if (workspaceId == null || workspaceId.isEmpty()) {
            workspaceId = List.of(CommandConfig.INSTANCE.getActiveWorkspace());
        }
        List<? extends CalendarCollection> calendarCollections = CommandConfig.INSTANCE.getCalendarStore()
                .getCollections(workspaceId.get(0));
        List<? extends CardCollection> cardCollections = CommandConfig.INSTANCE.getCardStore()
                .getCollections(workspaceId.get(0));
        List<String> b = new ArrayList<>();
        b.add("Calendar Collections:\n");
        b.addAll(calendarCollections.stream().map(this::printCollection).toList());
        b.add("\nCard Collections:\n");
        b.addAll(cardCollections.stream().map(this::printCollection).toList());
        getOutputHandler().accept(b);
        return 0;
    }

    private String printCollection(ObjectCollection<?> collection) {
        StringBuilder b = new StringBuilder();
        b.append("Collection: ").append(collection.getDisplayName()).append(" | ");
//        b.append("URI: ").append(collection.getUri()).append("\n");
        b.append("Size: ").append(collection.listObjectUIDs().size()).append(" | ");
        return b.toString();
    }
}
