package me.hubailmn.util.log;

import me.hubailmn.util.BaseBot;

public class CSend {

    public static void send(String message) {
        System.out.println(ColorFormat.colorize(message));
    }

    public static void prefixed(String message) {
        send("§7[§a" + BaseBot.getName() + "§7] " + message);
    }

    public static void info(String message) {
        prefixed("§e[INFO] §r" + message);
    }

    public static void warn(String message) {
        prefixed("§c[WARNING] §r" + message);
    }

    public static void error(String message) {
        String fullMessage = "[ERROR] " + message;
        prefixed("§4" + fullMessage);
        Log.error(fullMessage);
    }

    public static void debug(String message) {
        if (BaseBot.isDebug()) {
            String fullMessage = "[DEBUG] " + message;
            prefixed("§b" + fullMessage);
            Log.debug(fullMessage);
        }
    }

    public static void error(Throwable throwable) {
        if (throwable == null) {
            error("Unknown error (null throwable).");
            return;
        }

        error("Exception: " + throwable.getMessage());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            error("  at " + ste.toString());
            Log.error(throwable);
        }
    }

}
