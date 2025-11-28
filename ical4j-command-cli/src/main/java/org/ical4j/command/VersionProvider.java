package org.ical4j.command;

import net.fortuna.ical4j.model.Calendar;
import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {

    private final String commandVersion;

    private final String ical4jVersion;

    private final String javaVersion;

    public VersionProvider() {
        commandVersion = getClass().getPackage().getImplementationVersion();
        ical4jVersion = Calendar.class.getPackage().getImplementationVersion();
        javaVersion = System.getProperty("java.version");
    }

    @Override
    public String[] getVersion() {
        return new String[] {String.format("iCal4j Command %s (ical4j=%s, jvm=%s)",
                commandVersion, ical4jVersion, javaVersion)
        };
    }
}
