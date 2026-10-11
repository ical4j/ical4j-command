package org.ical4j.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * The {@code ict} version embedded at build time in {@code org/ical4j/command/version.properties}.
 * A resource rather than the jar manifest, because a native image and an in-process test run
 * have no manifest to read. Shared by both surfaces: {@code ict --version} and the MCP server info.
 */
public final class Version {

    static final String RESOURCE = "/org/ical4j/command/version.properties";

    private Version() {
    }

    /** The {@code ict} version, or {@code unknown} if the resource was not built. */
    public static String get() {
        try (InputStream in = Version.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return "unknown";
            }
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("version", "unknown");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
