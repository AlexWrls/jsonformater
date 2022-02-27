package ru.taa.jsonformater.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.JsonRs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class JsonToDataTableService {

    private static final Pattern NORMALIZE_JSON_PATTERN = Pattern.compile("([:,{\\[])(true|false|[0-9]+|null)([,}\\]])");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");


    public JsonRs format(String message) {
        try {
            String s = message.replaceAll("\\s|\\n", "");
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(normalizeJson(s));
            Map<String, String> map = new LinkedHashMap<>();
            List<String> path = new ArrayList<>();
            incrementValue(jsonObject, map, path, 0);
            StringBuilder sb = new StringBuilder();
            map.forEach((k, v) -> {
                sb.append("| ").append(k).append(" | ").append(v).append(" |").append("\n");

            });
            return JsonRs.builder().jsonData(sb.toString()).build();
        } catch (ParseException e) {
            return JsonRs.builder().jsonData("Ошибка разбора, проверьте данные").build();
        }

    }

    public void incrementValue(JSONObject obj, Map<String, String> map, List<String> path, int iObj) {
        Set<String> keys = obj.keySet();
//        ListIterator<String> iterator = new ArrayList(keys).listIterator(keys.size());
        for (String key : keys) {
//        while (iterator.hasPrevious()) {
//            final String key = iterator.previous();
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                incrementObject((JSONObject) value, key, path, map, iObj);
//                incrementValue((JSONObject) value, map, path, iObj);
            } else if (value instanceof JSONArray) {
                incrementArray(value, key, path, map, iObj);
            } else {

                String normalisePath = concatPath(path, key, iObj);
                map.put(normalisePath, value.toString());
            }

        }
    }

    private void incrementObject(JSONObject value, String key, List<String> path, Map<String, String> map, int iObj) {
        path.add(key);
        iObj++;
        if (path.size() > iObj) {
            path.subList(iObj - 1, path.size() - 1).clear();
        }
        if (iObj > path.size()) {
            iObj = path.size();
        }
        incrementValue(value, map, path, iObj);
    }

    private void incrementArray(Object value, String key, List<String> path, Map<String, String> map, int iObj) {
        JSONArray arr = (JSONArray) value;
        for (Object arrValue : arr) {
            if (arrValue instanceof JSONObject) {
                String normalisePath = getPath(key, path);
                path.add(normalisePath);
                iObj++;
                if (path.size() > iObj) {
                    path.subList(iObj - 1, path.size()).clear();
                }
                if (iObj > path.size()) {
                    iObj = path.size();
                }
                incrementValue((JSONObject) arrValue, map, path, iObj);
            } else if (arrValue instanceof String) {
                String normalisePath = getPath(key, path);
                path.add(normalisePath);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < path.size(); i++) {
                    if (i == path.size() - 1) {
                        sb.append(path.get(i));
                    } else {
                        sb.append(path.get(i)).append(".");
                    }
                }
                map.put(sb.toString(), arrValue.toString());
            } else if (arrValue instanceof JSONArray) {
                incrementArray(value, key, path, map, iObj);
            }
        }
    }

    private String getPath(String key, List<String> path) {
        int index = 0;
        if (!path.isEmpty()) {
            Matcher matcher = ARRAY_PATTERN.matcher(path.get(path.size() - 1));
            if (matcher.find()) {
                String elementName = matcher.group(1);
                index = Integer.parseInt(matcher.group(2));
                if (elementName.equals(key)) {
                    index++;
                } else {
                    index = 0;
                }
            }
        }
        if (index > 0) path.remove(path.size() - 1);
        return key + "[" + index + "]";
    }

    private String concatPath(List<String> path, String key, int iObj) {
        StringBuilder sb = new StringBuilder();
        if (path.size() > iObj) {
            path.subList(iObj, path.size()).clear();
        }
        path.forEach(i -> sb.append(i).append("."));
        sb.append(key);
        return sb.toString();
    }

    public String normalizeJson(String message) {
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
