package org.ical4j.command.ops;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads calendar and card input for handlers: from the {@code file} parameter when present,
 * otherwise from the context's standard input.
 */
final class Inputs {

    static final Param FILE = Param.path("file", "Read input from this file instead of standard input");

    private Inputs() {
    }

    static Calendar calendar(Context context, Params params) {
        try {
            Optional<Path> file = params.path("file");
            if (file.isPresent()) {
                try (InputStream in = Files.newInputStream(file.get())) {
                    return new CalendarBuilder().build(in);
                }
            }
            return new CalendarBuilder().build(context.stdin());
        } catch (NoSuchFileException e) {
            throw new OperationException.NotFound("No such file: " + e.getFile());
        } catch (IOException | ParserException e) {
            throw new OperationException.InvalidData("Cannot parse calendar input: " + e.getMessage(), e);
        }
    }

    static VCard card(Context context, Params params) {
        try {
            Optional<Path> file = params.path("file");
            if (file.isPresent()) {
                try (InputStream in = Files.newInputStream(file.get())) {
                    return new VCardBuilder(in).build();
                }
            }
            return new VCardBuilder(context.stdin()).build();
        } catch (NoSuchFileException e) {
            throw new OperationException.NotFound("No such file: " + e.getFile());
        } catch (IOException | ParserException e) {
            throw new OperationException.InvalidData("Cannot parse card input: " + e.getMessage(), e);
        }
    }
}
