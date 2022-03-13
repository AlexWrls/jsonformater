package ru.taa.jsonformater.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@UtilityClass
public class FormatUtils {

    private final static String NULL = "null";

    public static Map<String, String> prepareData(String data) {
        Map<String, String> tables = new LinkedHashMap<>();
        if (data.contains("\n")) {
            String[] lines = data.split("\n");
            for (String line : lines) {
                String[] item = line.split("\\|");
                tables.put(item[1].replaceAll("\\s|\\n", ""), item[2].replaceAll("\\s|\\n", ""));
            }
        }
        return tables;
    }

    @SneakyThrows
    public static String normalizeJson(String message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(message);
        parseObj(jsonObject);
        return jsonObject.toString();
    }

    private static void parseObj(JSONObject jsonObject) {
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            final Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                parseObj((JSONObject) value);
            } else if (value instanceof JSONArray) {
                parseArr((JSONArray) value);
            } else {
                if (!(value instanceof String)) {
                    jsonObject.put(key, Objects.isNull(value) ? NULL : value.toString());
                }
            }
        }
    }

    private static void parseArr(JSONArray json) {
        for (int i = 0; i < json.size(); i++) {
            final Object val = json.get(i);
            if (val instanceof JSONObject) {
                parseObj((JSONObject) val);
            } else if (val instanceof JSONArray) {
                parseArr((JSONArray) val);
            } else {
                if (!(val instanceof String)) {
                    json.set(i, Objects.isNull(val) ? NULL : val.toString());
                }
            }
        }
    }
}

























