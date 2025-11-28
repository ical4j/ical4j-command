package org.ical4j.command;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationReport;
import net.fortuna.ical4j.validate.ValidationResult;
import org.mnode.ical4j.serializer.JCalSerializer;
import org.mnode.ical4j.serializer.XCalSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Consumer;

/**
 * Default command consumer implementations.
 */
public interface DefaultOutputHandlers {

    /**
     * Writes command result to stdout.
     * @return a consumer instance
     * @param <T> command result type
     */
    static <T> Consumer<T> STDOUT_PRINTER() {
        return System.out::println;
    }

    /**
     * Write command result to the specified writer after validation.
     * @param out a writer use to output the result after validation
     * @return a consumer instance
     */
    static Consumer<Calendar> VALIDATING_CALENDAR_PRINTER(Writer out) {
        return calendar -> {
            try {
                new CalendarOutputter(true).output(calendar, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Write command result as text to the specified writer.
     * @param out a writer use to output the result
     * @return a consumer instance
     */
    static Consumer<ValidationResult> STDOUT_REPORT_PRINTER(Writer out) {
        return validationResult -> {
            try {
                new ValidationReport(ValidationReport.Format.TEXT).output(validationResult, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Writes command result to stdout.
     * @return a consumer instance
     * @param <T> command result type
     */
    static <T extends List<?>> Consumer<T> STDOUT_LIST_PRINTER() {
        return (t) -> t.forEach(System.out::println);
    }

    /**
     * Writes command result to stdout as JSON.
     * @return a consumer instance
     * @param <T> command result type
     */
    static <T> Consumer<T> STDOUT_JCAL_PRINTER() {
        return (t) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new JCalSerializer(null));

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(module);

            try {
                mapper.writeValue(System.out, t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Writes command result to stdout as XML.
     * @return a consumer instance
     * @param <T> command result type
     */
    static <T> Consumer<T> STDOUT_XCAL_PRINTER() {
        return (t) -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Calendar.class, new XCalSerializer(null));

            XmlMapper mapper = XmlMapper.builder().defaultUseWrapper(true).build();
            mapper.setConfig(mapper.getSerializationConfig().withRootName(
                            PropertyName.construct("icalendar", "urn:ietf:params:xml:ns:icalendar-2.0"))
                    .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));

            mapper.registerModule(module);

            try {
                mapper.writeValue(System.out, t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
