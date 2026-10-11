package org.ical4j.command.calendar;

import net.fortuna.ical4j.model.ComponentGroup;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.ical4j.command.DefaultOutputHandlers;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandLine.Command(name = "replace-uids", description = "Generate new UID properties for calendar components")
public class ReplaceUids extends AbstractCalendarCommand<List<CalendarComponent>> {

    public ReplaceUids() {
        super(DefaultOutputHandlers.STDOUT_LIST_PRINTER());
    }

    @Override
    public Integer call() throws Exception {
        UidGenerator uidGenerator = new RandomUidGenerator();
        List<CalendarComponent> updatedComponents = new ArrayList<>();
        List<Uid> processed = new ArrayList<>();
        for (CalendarComponent component : getCalendar().getComponents()) {
            Uid newUid = uidGenerator.generateUid();
            Optional<Uid> uid = component.getProperty(Property.UID);
            if (uid.isPresent() && !processed.contains(uid.get())) {
                ComponentGroup<?> componentGroup = new ComponentGroup<>(getCalendar().getComponents(), uid.get());
                updatedComponents.addAll(componentGroup.getRevisions().stream()
                        .map(c -> (CalendarComponent) c.replace(newUid))
                        .collect(Collectors.toList()));
                processed.add(uid.get());
            } else if (!uid.isPresent()) {
                updatedComponents.add(component.add(newUid));
            }
        }
        getOutputHandler().accept(updatedComponents);
        return 0;
    }
}
