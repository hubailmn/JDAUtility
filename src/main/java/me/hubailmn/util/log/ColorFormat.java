package me.hubailmn.util.log;

import java.util.HashMap;
import java.util.Map;

public class ColorFormat {

    public static final String RESET = "\u001B[0m";

    private static final Map<Character, String> colorMap = new HashMap<>();

    static {
        colorMap.put('0', "\u001B[30m"); // Black
        colorMap.put('1', "\u001B[34m"); // Dark Blue
        colorMap.put('2', "\u001B[32m"); // Dark Green
        colorMap.put('3', "\u001B[36m"); // Dark Aqua
        colorMap.put('4', "\u001B[31m"); // Dark Red
        colorMap.put('5', "\u001B[35m"); // Dark Purple
        colorMap.put('6', "\u001B[33m"); // Gold
        colorMap.put('7', "\u001B[37m"); // Gray
        colorMap.put('8', "\u001B[90m"); // Dark Gray
        colorMap.put('9', "\u001B[94m"); // Blue
        colorMap.put('a', "\u001B[92m"); // Green
        colorMap.put('b', "\u001B[96m"); // Aqua
        colorMap.put('c', "\u001B[91m"); // Red
        colorMap.put('d', "\u001B[95m"); // Light Purple
        colorMap.put('e', "\u001B[93m"); // Yellow
        colorMap.put('f', "\u001B[97m"); // White

        // Reset
        colorMap.put('r', RESET);
    }

    public static String colorize(String input) {
        StringBuilder builder = new StringBuilder();
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '§' && i + 1 < chars.length) {
                char code = chars[++i];
                String ansi = colorMap.getOrDefault(code, "");
                builder.append(ansi);
            } else {
                builder.append(chars[i]);
            }
        }

        builder.append(RESET);
        return builder.toString();
    }
}
