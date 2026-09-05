package org.ical4j.command.util;

/**
 * Utility methods for filesystem operations.
 */
public interface Filesystem {

    /**
     * Environment variable that overrides the platform data directory. Used by tests, the
     * native smoke suite and MCP client configurations that want an isolated store.
     */
    String DATA_DIR_ENV = "ICT_DATA_DIR";

    /**
     * Get the appropriate data directory: {@code ICT_DATA_DIR} if set, otherwise the platform
     * convention for the current operating system.
     * @return the data directory path
     */
    static String getDataDirectory() {
        String override = System.getenv(DATA_DIR_ENV);
        if (override != null && !override.isBlank()) {
            return override;
        }
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
