package bg.sofia.uni.project.movie.server.logger;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class LoggerUtil {
    private static String logFile = "server.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void setLogFile(String file) {
        if (file != null && !file.isBlank()) {
            logFile = file;
        }
    }

    public static void log(Exception e) {
        log(Level.SEVERE, e.getMessage(), e);
    }

    public static void log(Level level, String message, Throwable t) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("[" + LocalDateTime.now().format(formatter) + "] " + level);
            writer.println("Message: " + message);
            if (t != null) {
                writer.println("Error: " + t.getMessage());
                t.printStackTrace(writer);
            }
            writer.println("----------------------------------------");
            writer.flush();
        } catch (Exception e) {
            System.err.println("Failed to write to logger: " + e.getMessage());
            // Fallback to console
            e.printStackTrace();
        }
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    public static void error(String message, Throwable t) {
        log(Level.SEVERE, message, t);
    }
}