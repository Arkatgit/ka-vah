package ca.brock.cs.lambda;


import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("AppLogger"); // Or your application's root logger name
    private static boolean loggingEnabled = false; // Default to off

    public static void enableLogging(boolean enable) {
        loggingEnabled = enable;
        if (enable) {
            LOGGER.setLevel(Level.FINE); // Or your desired level
        } else {
            LOGGER.setLevel(Level.OFF);
        }
    }

    public static void fine(String message) {
        if (loggingEnabled && LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(message);
        }
    }

    public static void info(String message) {
        if (loggingEnabled && LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(message);
        }
    }

    // Add other logging levels as needed (warning, severe, etc.)
}
