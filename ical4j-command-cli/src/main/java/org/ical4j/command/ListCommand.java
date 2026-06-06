package org.ical4j.command;

import net.fortuna.ical4j.filter.ComponentFilter;
import net.fortuna.ical4j.filter.FilterExpressionParser;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import org.ical4j.command.config.CommandConfig;
import org.ical4j.connector.CalendarCollection;
import org.ical4j.connector.CardCollection;
import org.ical4j.connector.ObjectNotFoundException;
import org.ical4j.connector.ObjectStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@CommandLine.Command(name = "list", aliases = {"ls"}, description = "List objects in a collection")
public class ListCommand extends AbstractCommand<List<String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCommand.class);

    @CommandLine.Parameters(index = "0", description = "List context specification", arity = "0..1", split = ":")
    private String[] spec;

    @CommandLine.Option(names = "--filter", description = "Filter expression")
    private String filter;

    private final CommandConfig config;

    public ListCommand() {
        super(DefaultOutputHandlers.STDOUT_LIST_PRINTER());
        config = CommandConfig.INSTANCE;
    }

    public ListCommand(Consumer<List<String>> outputHandler) {
        this(outputHandler, CommandConfig.INSTANCE);
    }

    public ListCommand(Consumer<List<String>> outputHandler, CommandConfig config) {
        super(outputHandler);
        this.config = config;
    }

    @Override
    public Integer call() {
        try {
            List<String> result = new ArrayList<>();
            if (spec == null) {
                List<? extends CalendarCollection> calendarCollections = config.getCalendarStore()
                        .getCollections(config.getActiveWorkspace());
                List<? extends CardCollection> cardCollections = config.getCardStore()
                        .getCollections(config.getActiveWorkspace());
                Set<String> names = new HashSet<>();
                for (CalendarCollection collection : calendarCollections) {
                    names.add(collection.getDisplayName());
                }
                for (CardCollection collection : cardCollections) {
                    names.add(collection.getDisplayName());
                }
                result.addAll(names);
            } else {
                List<Calendar> calendars = config.getCalendarStore().getCollection(spec[0],
                        config.getActiveWorkspace()).getAll();

                List<CalendarComponent> components;
                if (spec.length > 1) {
                    String uid = spec[1];
                    components = calendars.stream().filter(
                            c -> c.getUid() != null && c.getUid().getValue().equals(uid)).findFirst().get().getComponents();
                    components.forEach(c -> result.add(printComponent(c)));
                } else {
                    if (filter != null) {
                        Predicate<? super CalendarComponent> filterPredicate = new ComponentFilter<>().predicate(
                                new FilterExpressionParser().parse(filter));
                        calendars = calendars.stream().filter(
                                c -> c.getComponents().stream().anyMatch(filterPredicate)).toList();
                    }
                    for (Calendar calendar : calendars) {
                        result.add(printCalendar(calendar));
                    }
                }
            }
            getOutputHandler().accept(result);
        } catch (ObjectStoreException | ObjectNotFoundException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }

    private String printCalendar(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.getUid().getValue());
        sb.append(" | ");
        sb.append(calendar.getComponents().get(0).getRequiredProperty("SUMMARY").getValue());
        sb.append(" | ");
        sb.append(calendar.getComponents().get(0).getRequiredProperty("DTSTART").getValue());
        return sb.toString();
    }

    private String printComponent(CalendarComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append(component.getUid().get().getValue());
        sb.append(" | ");
        sb.append(component.getProperty("SUMMARY"));
        sb.append(" | ");
        sb.append(component.getProperty("DTSTART") );
        return sb.toString();
    }
}
