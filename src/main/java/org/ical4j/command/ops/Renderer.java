package org.ical4j.command.ops;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.vcard.Entity;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardOutputter;
import org.mnode.ical4j.serializer.JCalSerializer;
import org.mnode.ical4j.serializer.JCardSerializer;
import org.mnode.ical4j.serializer.XCalSerializer;
import org.mnode.ical4j.serializer.XCardSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns a structured {@link Result} into text in a {@link Format}. Surface-neutral: the CLI
 * writes the string to stdout, MCP wraps it in a content block. Never touches a stream itself.
 */
public final class Renderer {

    private static final String NO_ERRORS = "No errors.";

    private final boolean pretty;

    private ObjectMapper json;

    private XmlMapper xml;

    public Renderer() {
        this(false);
    }

    public Renderer(boolean pretty) {
        this.pretty = pretty;
    }

    public String render(Result result, Format format) {
        switch (format) {
            case TEXT:
                return text(result);
            case JSON:
                return json(result);
            case XML:
                return xml(result);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    // ---- text -------------------------------------------------------------------------------

    private String text(Result result) {
        if (result instanceof Result.Status s) {
            return s.message() + "\n";
        }
        if (result instanceof Result.Identifiers ids) {
            return lines(ids.ids());
        }
        if (result instanceof Result.Descriptor d) {
            StringBuilder sb = new StringBuilder();
            d.attributes().forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
            return sb.toString();
        }
        if (result instanceof Result.Descriptors ds) {
            return table(ds.items());
        }
        if (result instanceof Result.CalendarData c) {
            return calendarText(c.calendar());
        }
        if (result instanceof Result.CardData c) {
            return cardText(c.card());
        }
        if (result instanceof Result.Validation v) {
            if (v.valid()) {
                return NO_ERRORS + "\n";
            }
            StringBuilder sb = new StringBuilder();
            for (Result.ValidationEntry e : v.entries()) {
                sb.append("- ").append(e.severity()).append(": ").append(e.context())
                        .append(" - ").append(e.message()).append('\n');
            }
            return sb.toString();
        }
        throw new IllegalArgumentException("Unknown result type: " + result.getClass());
    }

    private static String lines(List<String> items) {
        StringBuilder sb = new StringBuilder();
        items.forEach(i -> sb.append(i).append('\n'));
        return sb.toString();
    }

    /** Aligned columns with a header row; the column set is the ordered union of all keys. */
    static String table(List<Result.Descriptor> items) {
        if (items.isEmpty()) {
            return "";
        }
        Set<String> columns = new LinkedHashSet<>();
        items.forEach(i -> columns.addAll(i.attributes().keySet()));
        List<String> cols = new ArrayList<>(columns);
        int[] widths = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            widths[i] = cols.get(i).length();
            for (Result.Descriptor d : items) {
                widths[i] = Math.max(widths[i], d.attributes().getOrDefault(cols.get(i), "").length());
            }
        }
        StringBuilder sb = new StringBuilder();
        row(sb, cols, widths);
        for (Result.Descriptor d : items) {
            List<String> values = new ArrayList<>();
            cols.forEach(c -> values.add(d.attributes().getOrDefault(c, "")));
            row(sb, values, widths);
        }
        return sb.toString();
    }

    private static void row(StringBuilder sb, List<String> cells, int[] widths) {
        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i);
            if (i < cells.size() - 1) {
                sb.append(cell).append(" ".repeat(widths[i] - cell.length() + 2));
            } else {
                sb.append(cell);
            }
        }
        sb.append('\n');
    }

    private static String calendarText(Calendar calendar) {
        try {
            StringWriter out = new StringWriter();
            new CalendarOutputter(false).output(calendar, out);
            return out.toString();
        } catch (IOException e) {
            throw new OperationException("Cannot write calendar", e);
        }
    }

    private static String cardText(VCard card) {
        try {
            StringWriter out = new StringWriter();
            new VCardOutputter(false).output(card, out);
            return out.toString();
        } catch (IOException e) {
            throw new OperationException("Cannot write card", e);
        }
    }

    // ---- json -------------------------------------------------------------------------------

    private String json(Result result) {
        Object value;
        if (result instanceof Result.Status s) {
            value = Map.of("status", s.message());
        } else if (result instanceof Result.Identifiers ids) {
            value = ids.ids();
        } else if (result instanceof Result.Descriptor d) {
            value = new LinkedHashMap<>(d.attributes());
        } else if (result instanceof Result.Descriptors ds) {
            List<Map<String, String>> items = new ArrayList<>();
            ds.items().forEach(d -> items.add(new LinkedHashMap<>(d.attributes())));
            value = items;
        } else if (result instanceof Result.CalendarData c) {
            value = c.calendar();
        } else if (result instanceof Result.CardData c) {
            // jCard is defined per card; a VCard object is a container of entities.
            List<Entity> entities = c.card().getEntities();
            value = entities.size() == 1 ? entities.get(0) : entities;
        } else if (result instanceof Result.Validation v) {
            List<Map<String, String>> entries = new ArrayList<>();
            for (Result.ValidationEntry e : v.entries()) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("severity", e.severity());
                m.put("context", e.context());
                m.put("message", e.message());
                entries.add(m);
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("valid", v.valid());
            m.put("entries", entries);
            value = m;
        } else {
            throw new IllegalArgumentException("Unknown result type: " + result.getClass());
        }
        try {
            ObjectMapper mapper = jsonMapper();
            return (pretty ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer()).writeValueAsString(value) + "\n";
        } catch (JsonProcessingException e) {
            throw new OperationException("Cannot write JSON", e);
        }
    }

    private ObjectMapper jsonMapper() {
        if (json == null) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new JCalSerializer(Calendar.class));
            module.addSerializer(Entity.class, new JCardSerializer(Entity.class));
            json = new ObjectMapper().registerModule(module);
        }
        return json;
    }

    // ---- xml --------------------------------------------------------------------------------

    private String xml(Result result) {
        try {
            if (result instanceof Result.CalendarData c) {
                return xmlMapper("icalendar", "urn:ietf:params:xml:ns:icalendar-2.0")
                        .writeValueAsString(c.calendar()) + "\n";
            }
            if (result instanceof Result.CardData c) {
                List<Entity> entities = c.card().getEntities();
                return xmlMapper("vcards", "urn:ietf:params:xml:ns:vcard-4.0")
                        .writeValueAsString(entities.size() == 1 ? entities.get(0) : entities) + "\n";
            }
        } catch (JsonProcessingException e) {
            throw new OperationException("Cannot write XML", e);
        }
        throw new OperationException.Usage("XML output is only available for calendar and card data");
    }

    private XmlMapper xmlMapper(String rootName, String namespace) {
        if (xml == null) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new XCalSerializer(Calendar.class));
            module.addSerializer(Entity.class, new XCardSerializer(Entity.class));
            xml = XmlMapper.builder().defaultUseWrapper(true).build();
            xml.registerModule(module);
        }
        xml.setConfig(xml.getSerializationConfig().withRootName(PropertyName.construct(rootName, namespace))
                .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
        return xml;
    }
}
