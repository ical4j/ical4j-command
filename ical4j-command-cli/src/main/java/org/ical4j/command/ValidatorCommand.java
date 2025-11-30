package org.ical4j.command;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.validate.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;

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
@CommandLine.Command(name = "validate", description = "Validate iCalendar and vCard files")
public class ValidatorCommand extends AbstractCommand<ValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorCommand.class);

    public enum ValidationMode {
        ICALENDAR,
        VCARD
    }

    /**
     * Input options.
     */
    @CommandLine.ArgGroup(multiplicity = "1")
    private InputOptions input;

    @CommandLine.Option(names = "--mode", interactive = true, arity = "0..1",
            description = "Valid values: ${COMPLETION-CANDIDATES}", defaultValue = "ICALENDAR")
    private ValidationMode mode;

    @CommandLine.Option(names = "--lenient", description = "Enable lenient parsing")
    private boolean lenient;

    @Override
    public Integer call() throws Exception {
        if (lenient) {
            System.setProperty("ical4j.parsing.relaxed", "true");
        }
        try {
            ValidationResult result;
            switch (mode) {
                case VCARD -> {
                    result = input.toVCard().validate();
                }
                default -> {
                    result = input.toCalendar().validate();
                }
            }
            if (result.hasErrors()) {
                getOutputHandler().accept(result);
            } else {
                System.out.print("No errors.");
            }
        } catch (IOException | ParserException e) {
            LOGGER.error("Unexpected error", e);
            return 1;
        }
        return 0;
    }
}
