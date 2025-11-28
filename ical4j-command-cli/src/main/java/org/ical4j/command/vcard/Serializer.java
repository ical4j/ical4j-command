package org.ical4j.command.vcard;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.vcard.VCard;
import org.mnode.ical4j.serializer.JCardSerializer;
import org.mnode.ical4j.serializer.XCardSerializer;
import org.mnode.ical4j.serializer.jmap.JSCardSerializer;
import org.mnode.ical4j.serializer.jotn.VCardSerializer;
import org.mnode.ical4j.serializer.jsonld.PersonJsonLdSerializer;
import picocli.CommandLine;

@CommandLine.Command(name = "serialize", description = "Serialize a specified vCard")
public class Serializer extends AbstractCardCommand<String> {

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

        Object value = getCard();
        if (pretty) {
            getOutputHandler().accept(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value));
        } else {
            getOutputHandler().accept(mapper.writeValueAsString(value));
        }

        return 0;
    }

    private ObjectMapper serializeJson() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VCard.class, new JCardSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeXml() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VCard.class, new XCardSerializer(null));
        XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(
                        PropertyName.construct("vcard", "urn:ietf:params:xml:ns:vcard-4.0"))
                .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeAtom() {
        SimpleModule module = new SimpleModule();
//        module.addSerializer(Calendar.class, new AtomFeedCalendarSerializer(null));
        XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(
                        PropertyName.construct("feed", "https://www.w3.org/2005/Atom"))
                .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJotn(String[] filter) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VCard.class, new VCardSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJmap() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VCard.class, new JSCardSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    private ObjectMapper serializeJsonld() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(VCard.class, new PersonJsonLdSerializer(null));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }
}
