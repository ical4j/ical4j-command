package org.ical4j.command;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.fortuna.ical4j.model.Calendar;
import org.mnode.ical4j.serializer.JCalSerializer;
import org.mnode.ical4j.serializer.XCalSerializer;
import picocli.CommandLine;

/*
 * Copyright (c) 2025, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@CommandLine.Command(name = "convert", description = "Convert iCalendar and vCard files to other formats")
public class ConverterCommand extends AbstractCommand<String> {

    public enum Format {
        JCAL, XCAL,
        JCARD, XCARD,
        JSCALENDAR, JSCARD,
        JSONLD, RDF
    }

    /**
     * Input options.
     */
    @CommandLine.ArgGroup(multiplicity = "1")
    private InputOptions input;

    @CommandLine.Option(names = "--format", interactive = true, arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "JCAL")
    private Format format;

    @CommandLine.Option(names = "--pretty", description = "Enable pretty printing of output")
    private boolean prettyPrint;

    @Override
    public Integer call() throws Exception {
        ObjectMapper objectMapper;
        switch (format) {
            case XCAL -> {
                SimpleModule module = new SimpleModule();
                module.addSerializer(Calendar.class, new XCalSerializer(null));
                objectMapper = new XmlMapper();
                objectMapper.setConfig(objectMapper.getSerializationConfig().withRootName(
                                PropertyName.construct("icalendar",
                                        "urn:ietf:params:xml:ns:icalendar-2.0"))
                        .with(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
                objectMapper.registerModule(module);
            }
            default -> {
                SimpleModule module = new SimpleModule();
                module.addSerializer(Calendar.class, new JCalSerializer(null));
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(module);
            }
        }
        if (prettyPrint) {
            getOutputHandler().accept(objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(input.toCalendar()));
        } else {
            getOutputHandler().accept(objectMapper.writeValueAsString(input.toCalendar()));
        }
        return 0;
    }
}
