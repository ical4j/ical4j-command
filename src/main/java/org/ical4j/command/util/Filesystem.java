package org.ical4j.command.util;

/**
 * Utility methods for filesystem operations.
 */
public interface Filesystem {

    /**
     * Get the appropriate data directory for the current operating system.
     * @return the data directory path
     */
    static String getDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support";
        } else {
            return userHome + "/.local/share";
        }
    }
}
