package ru.taa.jsonformater.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.JsonRs;
import ru.taa.jsonformater.utils.FormatUtils;


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

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");

    public JsonRs format(String message) {
        try {
            String normalizeJson = FormatUtils.normalizeJson(message);
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(normalizeJson);
            Map<String, String> map = new LinkedHashMap<>();
            List<String> path = new ArrayList<>();
            parse(jsonObject, map, path, 0);
            StringBuilder sb = new StringBuilder();
            map.forEach((k, v) -> {
                sb.append("| ").append(k).append(" | ").append(v).append(" |").append("\n");
            });
            return JsonRs.builder().jsonData(sb.toString()).build();
        } catch (ParseException e) {
            return JsonRs.builder().jsonData("Ошибка разбора, проверьте данные").build();
        }

    }

    public void parse(JSONObject obj, Map<String, String> map, List<String> path, int iObj) {
        Set<String> keys = obj.keySet();
//        ListIterator<String> iterator = new ArrayListkeys).listIterator(keys.size());
        for (String key : keys) {
//        while (iterator.hasPrevious()) {
//            final String key = iterator.previous();
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                parseJsonObject((JSONObject) value, key, path, map, iObj);
            } else if (value instanceof JSONArray) {
                parseJsonArray(value, key, path, map, iObj);
            } else {
                correctPath(path);
                String normalisePath = concatPath(path, key, iObj);
                map.put(normalisePath, value.toString());
            }

        }
    }

    private void parseJsonObject(JSONObject value, String key, List<String> path, Map<String, String> map, int iObj) {
        path.add(key);
        iObj++;
        if (path.size() > iObj) {
            path.subList(iObj - 1, path.size() - 1).clear();
        }
        iObj = Math.min(iObj, path.size());
        parse(value, map, path, iObj);
    }

    private void parseJsonArray(Object value, String key, List<String> path, Map<String, String> map, int iObj) {
        JSONArray arr = (JSONArray) value;
        for (Object arrValue : arr) {
            if (arrValue instanceof JSONObject) {
                String normalisePath = getPathFromKey(key, path);
                path.add(normalisePath);
                iObj++;
                if (path.size() > iObj) {
                    path.subList(iObj - 1, path.size() - 1).clear();
                }
                iObj = Math.min(iObj, path.size());
                correctPath(path);
                parse((JSONObject) arrValue, map, path, iObj);
            } else if (arrValue instanceof String) {
                String normalisePath = getPathFromKey(key, path);
                path.add(normalisePath);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < path.size() - 1; i++) {
                    sb.append(path.get(i)).append(".");
                }
                sb.append(normalisePath);
                map.put(sb.toString(), arrValue.toString());
            } else if (arrValue instanceof JSONArray) {
                parseJsonArray(value, key, path, map, iObj);
            }
        }
    }

    private String getPathFromKey(String key, List<String> path) {
        int index = 0;
        if (!path.isEmpty()) {
            correctPath(path);
            Matcher matcher = ARRAY_PATTERN.matcher(path.get(path.size() - 1));
            if (matcher.find()) {
                String elementName = matcher.group(1);
                index = elementName.equals(key)
                        ? Integer.parseInt(matcher.group(2)) + 1
                        : 0;
            }
        }
        if (index > 0) path.remove(path.size() - 1);
        return key + "[" + index + "]";
    }

    private void correctPath(List<String> path) {
        if (path.size() > 1) {
            for (int i = 0; i < path.size(); i++) {
                for (int j = 0; j < path.size(); j++) {
                    if (i != j) {
                        String path1 = path.get(i);
                        String path2 = path.get(j);
                        Matcher matcher1 = ARRAY_PATTERN.matcher(path1);
                        if (matcher1.find()) {
                            path1 = matcher1.group(1);
                            int index1 = Integer.parseInt(matcher1.group(2));
                            Matcher matcher2 = ARRAY_PATTERN.matcher(path2);
                            if (matcher2.find()) {
                                path2 = matcher2.group(1);
                                if (path1.equals(path2)) {
                                    path.set(i, path1 + "[" + (index1 + 1) + "]");
                                    path.subList(i + 1, path.size()).clear();
                                }
                            }
                        }
                    }
                }
            }
        }
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
}
