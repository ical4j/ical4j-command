package org.ical4j.command;

import net.fortuna.ical4j.data.ParserException;
import org.ical4j.command.config.CommandConfig;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.FailedOperationException;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "import", description = "Update an object collection")
public class ImportCalendars extends AbstractCommand<String> {

    @CommandLine.ArgGroup(multiplicity = "1")
    private InputOptions input;

    @CommandLine.Parameters(index = "0", description = "Name of the destination collection")
    private String collectionName;

    @Override
    public Integer call() {
        try {
            CalendarCollection collection = CommandConfig.INSTANCE.getCalendarStore().getCollection(collectionName,
                    CommandConfig.INSTANCE.getActiveWorkspace());
            collection.merge(input.toCalendar());
        } catch (ObjectNotFoundException e) {
            getOutputHandler().accept("Collection with name '" + collectionName + "' does not exist");
            return 1;
        } catch (ObjectStoreException | ParserException | FailedOperationException | IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
