package org.ical4j.command.cli;

import net.fortuna.ical4j.model.Calendar;
import org.ical4j.command.Version;
import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        String ical4jVersion = Calendar.class.getPackage().getImplementationVersion();
        return new String[] {String.format("ict %s (ical4j=%s, java=%s)", Version.get(),
                ical4jVersion == null ? "unknown" : ical4jVersion, System.getProperty("java.version"))
        };
    }
}
