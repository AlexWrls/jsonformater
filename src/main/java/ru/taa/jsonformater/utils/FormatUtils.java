package ru.taa.jsonformater.utils;

import lombok.SneakyThrows;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Objects;
import java.util.Set;


public class FormatUtils {
    //    private static final Pattern NORMALIZE_JSON_PATTERN = Pattern.compile("([:,{\\[])(true|false|(\\d+)|null)([,}\\]])");
//
//    public static String normalizeJson(String message) {
//        String body = message.replaceAll("\\s|\\n", "");
//        return replace(body);
//
//    }
//
//    private String replace(String message){
//        StringBuffer buffer = new StringBuffer();
//        Matcher matcher = NORMALIZE_JSON_PATTERN.matcher(message);
//        if (matcher.find()) {
//            String formatString = String.format("%s\"%s\"%s", matcher.group(1), matcher.group(2), matcher.group(3));
//            matcher.appendReplacement(buffer, formatString);
//            matcher.appendTail(buffer);
//            replace(buffer.toString());
//        }
//        return message;
//    }
    private static final String RES = "{\n" +
            "    \"persons\": [\n" +
            "        {\n" +
            "            \"bio\": {\n" +
            "                \"birthDay\": \"01.02.2222\"\n" +
            "            }\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"Ivan\",\n" +
            "            \"lastName\": null,\n" +
            "            \"arr\": [\n" +
            "                1,\n" +
            "                2,\n" +
            "                3\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"status\": true\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static void main(String[] args) {
        System.out.println(normalizeJson(RES));
    }


    @SneakyThrows
    public static String normalizeJson(String message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(message);
        parseObj(jsonObject);
        return jsonObject.toString();
    }

    public static void parseObj(JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            final Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                parseObj((JSONObject) value);
            } else if (value instanceof JSONArray) {
                parseArr((JSONArray) value);
            } else {
                if (!(value instanceof String)) {
                    jsonObject.put(key, Objects.isNull(value) ? "null" : value.toString());
                }
            }
        }
    }

    public static void parseArr(JSONArray json) {
        for (int i = 0; i < json.size(); i++) {
            final Object val = json.get(i);
            if (val instanceof JSONObject) {
                parseObj((JSONObject) val);
            } else if (val instanceof JSONArray) {
                parseArr((JSONArray) val);
            } else {
                if (!(val instanceof String)) {
                    json.set(i, Objects.isNull(val) ? "null" : val.toString());
                }
            }
        }
    }
}

























