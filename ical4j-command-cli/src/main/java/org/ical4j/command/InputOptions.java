package org.ical4j.command;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.Calendars;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import picocli.CommandLine;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * Support for object loading via various input options.
 */
public class InputOptions {

    @CommandLine.Option(names = {"-url"}, required = true)
    private URL url;

    @CommandLine.Option(names = {"-file"}, required = true)
    private String filename;

    @CommandLine.Option(names = {"-", "--stdin"}, required = true)
    private boolean stdin;

    public boolean hasUrl() {
        return url != null;
    }

    public URL getUrl() {
        return url;
    }

    public boolean hasFilename() {
        return filename != null;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isStdin() {
        return stdin;
    }

    public Calendar toCalendar() throws ParserException, IOException {
        Calendar calendar = null;
        if (hasFilename()) {
            calendar = Calendars.load(getFilename());
        } else if (hasUrl()) {
            calendar = Calendars.load(getUrl());
        } else if (isStdin()) {
            final CalendarBuilder builder = new CalendarBuilder();
            calendar = builder.build(System.in);
        }
        return calendar;
    }

    public VCard toVCard() throws ParserException, IOException {
        VCard card = null;
        if (hasFilename()) {
            card = new VCardBuilder(new FileReader(getFilename())).build();
        } else if (hasUrl()) {
            card = new VCardBuilder(getUrl().openStream()).build();
        } else if (isStdin()) {
            card = new VCardBuilder(System.in).build();
        }
        return card;
    }
}
