package me.hubailmn.util.log;

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

    public static void error(String e) {
        writeToFile(errorFile, e);
    }

    public static void error(Throwable e) {
        writeToFile(errorFile, formatError(e));
    }

    public static void debug(String message) {
        writeToFile(debugFile, formatMessage("DEBUG", message));
    }

    public static void log(String message) {
        writeToFile(logFile, formatMessage("INFO", message));
    }

    private static String formatError(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return "[%s] ERROR:\n%s\n".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                sw
        );
    }

    private static String formatMessage(String level, String message) {
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
}
