package org.ical4j.command.calendar;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentGroup;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Uid;
import org.mnode.ical4j.serializer.JCalSerializer;
import org.mnode.ical4j.serializer.XCalSerializer;
import org.mnode.ical4j.serializer.atom.AtomFeedCalendarSerializer;
import org.mnode.ical4j.serializer.jmap.JSEventSerializer;
import org.mnode.ical4j.serializer.jmap.JSGroupSerializer;
import org.mnode.ical4j.serializer.jotn.CalendarSerializer;
import org.mnode.ical4j.serializer.jotn.component.VEventSerializer;
import org.mnode.ical4j.serializer.jotn.component.VTimeZoneSerializer;
import org.mnode.ical4j.serializer.jsonld.EventJsonLdSerializer;
import picocli.CommandLine;

@CommandLine.Command(name = "serialize", description = "Serialize a specified calendar or embedded component")
public class Serializer extends AbstractCalendarCommand<String> {

    public enum FormatOption {
        json, xml, jotn, atom, jmap, jsonld;
    }

    @CommandLine.Option(names = {"-format"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    private FormatOption format;

    @CommandLine.Option(names = {"-pretty"})
    private boolean pretty;

    @CommandLine.Option(names = {"-uid"})
    private String uid;

    @CommandLine.Option(names = {"-filter"}, split = ",")
    private String[] filter;

    @Override
    public Integer call() throws Exception {
        ObjectMapper mapper;
        if (FormatOption.xml.equals(format)) {
            mapper = serializeXml();
        } else if (FormatOption.atom.equals(format)) {
            mapper = serializeAtom();
        } else if (FormatOption.jotn.equals(format)) {
            mapper = serializeJotn(filter);
        } else if (FormatOption.jmap.equals(format)) {
            mapper = serializeJmap();
        } else if (FormatOption.jsonld.equals(format)) {
            mapper = serializeJsonld();
        } else {
            mapper = serializeJson();
        }

        Object value;
        if (uid != null) {
            ComponentGroup<?> componentGroup = new ComponentGroup<>(getCalendar().getComponents(), new Uid(uid));
            value = componentGroup.getRevisions();
        } else {
            value = getCalendar();
        }
        if (pretty) {
            getOutputHandler().accept(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
        } else {
            getOutputHandler().accept(mapper.writeValueAsString(value));
        }

        return 0;
    }

    private ObjectMapper serializeJson() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Calendar.class, new JCalSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeXml() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Calendar.class, new XCalSerializer(null));
        XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(
                        PropertyName.construct("icalendar", "urn:ietf:params:xml:ns:icalendar-2.0"))
                .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeAtom() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Calendar.class, new AtomFeedCalendarSerializer(null));
        XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(
                        PropertyName.construct("feed", "https://www.w3.org/2005/Atom"))
                .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJotn(String[] filter) {
        SimpleModule module = new SimpleModule();
        if (filter != null) {
            module.addSerializer(Calendar.class, new CalendarSerializer(null, filter));
        } else {
            module.addSerializer(Calendar.class, new CalendarSerializer(null));
            module.addSerializer(VEvent.class, new VEventSerializer(null));
            module.addSerializer(VTimeZone.class, new VTimeZoneSerializer(null));
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJmap() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Calendar.class, new JSGroupSerializer(null));
        module.addSerializer(VEvent.class, new JSEventSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJsonld() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VEvent.class, new EventJsonLdSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }
}
