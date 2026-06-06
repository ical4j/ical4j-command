package org.ical4j.command.calendar;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.validate.ValidationResult;
import org.ical4j.command.DefaultOutputHandlers;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;

@CommandLine.Command(name = "validator", description = "Produce a validation report for input data")
public class Validator extends AbstractCalendarCommand<ValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    public Validator() {
        super(DefaultOutputHandlers.STDOUT_REPORT_PRINTER(new PrintWriter(System.out, true)));
    }

    @Override
    public Integer call() {
        try {
            ValidationResult result = getCalendar().validate();
            if (result.hasErrors()) {
                getOutputHandler().accept(result);
            } else {
                System.out.print("No errors.");
            }
        } catch (IOException | ParserException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
