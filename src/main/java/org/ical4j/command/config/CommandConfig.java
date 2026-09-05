package org.ical4j.command.config;

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

import com.electronwill.nightconfig.core.file.FileConfig;
import org.ical4j.command.util.Filesystem;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectStore;
import org.ical4j.connector.local.LocalCalendarStore;
import org.ical4j.connector.local.LocalCardStore;

import java.io.File;

/**
 * Persists command configuration.
 */
public class CommandConfig {

    public static final CommandConfig INSTANCE = new CommandConfig();

    private final FileConfig fileConfig;

    public CommandConfig() {
        this(FileConfig.builder(new File(Filesystem.getDataDirectory(), "iCal4j/ictrc.toml"))
                .defaultResource("/ictrc.default").autosave().sync().build());
    }

    public CommandConfig(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    public String get(String key, String defaultValue) {
        fileConfig.load();
        return fileConfig.getOrElse(key, defaultValue);
    }

    public String set(String key, String value) {
        String previous = get(key, null);
        fileConfig.set(key, value);
        return previous;
    }

    public String getUserAgent() {
        return get("user.agent", "iCal4j-Command-Line/1.0");
    }

    public String getUserEmail() {
        return get("user.email", "");
    }

    public String setUserEmail(String email) {
        return set("user.email", email);
    }

    public String getUserName() {
        return get("user.name", "");
    }

    public String setUserName(String name) {
        return set("user.name", name);
    }

    public String getActiveWorkspace() {
        return get("workspace.active", "default");
    }

    public String setActiveWorkspace(String workspaceId) {
        return set("workspace.active", workspaceId);
    }

    public ObjectStore<? extends CalendarCollection> getCalendarStore() {
        return new LocalCalendarStore(new File(Filesystem.getDataDirectory(),
                "iCal4j/" + get("store.calendar.path", "calendars")));
    }

    public String setCalendarStorePath(String path) {
        return set("store.calendar.path", path);
    }

    public ObjectStore<? extends CardCollection> getCardStore() {
        return new LocalCardStore(new File(Filesystem.getDataDirectory(), "iCal4j/" +
                get("store.card.path", "vcards")));
    }

    public String setCardStorePath(String path) {
        return set("store.card.path", path);
    }

    public String getDefaultContentType(String concept) {
        return get("contentType." + concept,
                get("contentType.default", "text/plain"));
    }

    public String setDefaultContentType(String concept, String contentType) {
        return set("contentType." + concept, contentType);
    }

    public String setDefaultContentType(String contentType) {
        return set("contentType.default", contentType);
    }
}
