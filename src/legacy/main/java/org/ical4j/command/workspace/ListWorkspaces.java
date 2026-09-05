package org.ical4j.command.workspace;

import org.ical4j.command.AbstractCommand;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.command.config.CommandConfig;
import picocli.CommandLine;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
@CommandLine.Command(name = "list-workspaces", aliases = {"lws"}, description = "List available workspaces")
public class ListWorkspaces extends AbstractCommand<Set<String>> {

    public ListWorkspaces() {
        super(DefaultOutputHandlers.STDOUT_LIST_PRINTER());
    }

    @Override
    public Integer call() throws Exception {
        Set<String> workspaces = new TreeSet<>();
        workspaces.add(CommandConfig.INSTANCE.getActiveWorkspace() + " *");
        workspaces.addAll(format(CommandConfig.INSTANCE.getCalendarStore().listWorkspaceIds()));
        workspaces.addAll(format(CommandConfig.INSTANCE.getCardStore().listWorkspaceIds()));
        getOutputHandler().accept(workspaces);
        return 0;
    }

    private List<String> format(List<String> ids) {
        return ids.stream().map(id -> {
            if (CommandConfig.INSTANCE.getActiveWorkspace().equals(id)) {
                return id + " *";
            } else {
                return id;
            }
        }).toList();
    }
}
