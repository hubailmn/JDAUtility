package me.hubailmn.util.log;

import java.util.HashMap;
import java.util.Map;

public class ColorFormat {

    public static final String RESET = "\u001B[0m";

    private static final Map<Character, String> colorMap = new HashMap<>();

    static {
        colorMap.put('0', "\u001B[30m");
        colorMap.put('1', "\u001B[34m");
        colorMap.put('2', "\u001B[32m");
        colorMap.put('3', "\u001B[36m");
        colorMap.put('4', "\u001B[31m");
        colorMap.put('5', "\u001B[35m");
        colorMap.put('6', "\u001B[33m");
        colorMap.put('7', "\u001B[37m");
        colorMap.put('8', "\u001B[90m");
        colorMap.put('9', "\u001B[94m");
        colorMap.put('a', "\u001B[92m");
        colorMap.put('b', "\u001B[96m");
        colorMap.put('c', "\u001B[91m");
        colorMap.put('d', "\u001B[95m");
        colorMap.put('e', "\u001B[93m");
        colorMap.put('f', "\u001B[97m");

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
