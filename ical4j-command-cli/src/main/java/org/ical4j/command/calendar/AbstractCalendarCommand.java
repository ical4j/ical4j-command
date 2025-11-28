package org.ical4j.command.calendar;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.ical4j.command.AbstractCommand;
import org.ical4j.command.InputOptions;
import picocli.CommandLine;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Provides basis for commands that operate on a single calendar input. This may include things like creation
 * or import operations.
 *
 * @param <T> the command result consumer
 */
public abstract class AbstractCalendarCommand<T> extends AbstractCommand<T> {

    /**
     * One and only one input option required when invoked via command line.
     */
    @CommandLine.ArgGroup(multiplicity = "1")
    protected InputOptions input;

    /**
     * Target calendar object loaded via specified input on command line, or explicitly set otherwise.
     */
    private Calendar calendar;

    public AbstractCalendarCommand() {
    }

    public AbstractCalendarCommand(Consumer<T> consumer) {
        super(consumer);
    }

    /**
     * Set the target of the command execution.
     * @param calendar
     * @return
     */
    public AbstractCalendarCommand<T> withCalendar(Calendar calendar) {
        this.calendar = calendar;
        return this;
    }

    public Calendar getCalendar() throws ParserException, IOException {
        if (calendar == null) {
            calendar = input.toCalendar();
        }
        return calendar;
    }
}
