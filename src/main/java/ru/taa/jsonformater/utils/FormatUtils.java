package ru.taa.jsonformater.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class FormatUtils {
    private static final Pattern NORMALIZE_JSON_PATTERN = Pattern.compile("([:,{\\[])(true|false|[0-9]+|null)([,}\\]])");

    public static String normalizeJson(String message) {
        String body = message.replaceAll("\\s|\\n", "");
        Matcher matcher = NORMALIZE_JSON_PATTERN.matcher(body);
        if (!matcher.find()) {
            return body;
        }
        StringBuffer buffer = new StringBuffer();
        do {
            String formatString = String.format("%s\"%s\"%s", matcher.group(1), matcher.group(2), matcher.group(3));
            matcher.appendReplacement(buffer, formatString);
        } while (matcher.find());
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
