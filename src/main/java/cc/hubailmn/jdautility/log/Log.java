package cc.hubailmn.jdautility.log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final Path configPath = Path.of("logs");

    private static final File errorFile;
    private static final File debugFile;
    private static final File logFile;

    static {
        File configDir = ensureConfigDirectory().toFile();

        errorFile = new File(configDir, "Error-Logs.txt");
        debugFile = new File(configDir, "Debug-Logs.txt");
        logFile = new File(configDir, "Logs.txt");

        try {
            if (!errorFile.exists()) errorFile.createNewFile();
            if (!debugFile.exists()) debugFile.createNewFile();
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException e) {
            CSend.error("Failed to create log files in: " + configDir.getAbsolutePath());
            CSend.error(e);
        }
    }

    public static Path ensureConfigDirectory() {
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }
        } catch (IOException e) {
            CSend.error("Failed to create log directory at: " + configPath.toAbsolutePath());
            CSend.error(e);
        }
        return configPath;
    }

    public static void error(String message) {
        writeToFile(errorFile, formatMessage(LogLevel.ERROR, message));
    }

    public static void debug(String message) {
        writeToFile(debugFile, formatMessage(LogLevel.DEBUG, message));
    }

    public static void log(String message) {
        writeToFile(logFile, formatMessage(LogLevel.INFO, message));
    }

    public static void error(Throwable e) {
        writeToFile(errorFile, formatThrowable(e));
    }

    private static String formatThrowable(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return "[%s] ERROR: %s\n".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                sw.toString()
        );
    }

    private static String formatMessage(LogLevel level, String message) {
        return "[%s] %s: %s\n".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                level,
                message
        );
    }

    private static void writeToFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
        } catch (IOException e) {
            CSend.error("Failed to write to log file: " + file.getAbsolutePath());
            CSend.error(e);
        }
    }

    public enum LogLevel {
        INFO, WARN, DEBUG, ERROR
    }
}
