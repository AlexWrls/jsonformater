package ru.taa.jsonformater.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.utils.FormatUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Сервис конвертирования Json в Data table
 */
@Service
public class JsonToDataTableService {

    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат JSON";
    private static final String EMPTY_ARRAY = "[]";
    private static final String EMPTY_OBJECT = "{}";

    public ObjectRs convert(String jsonString) {
        try {
            String normalizeJson = FormatUtils.normalizeJson(jsonString);
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(normalizeJson);
            Map<String, String> map = new LinkedHashMap<>();
            parseObj(jsonObject, new ArrayList<>(), map);
            StringBuilder sb = new StringBuilder();
            map.forEach((k, v) -> {
                sb.append("| ").append(k).append(" | ").append(v).append(" |").append("\n");
            });

            return ObjectRs.builder().txt(sb.toString()).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }

    }

    private static void parseObj(JSONObject jsonObj, List<String> path, Map<String, String> map) {
        Set<String> keys = jsonObj.keySet();
        for (String key : keys) {
            Object value = jsonObj.get(key);
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                path.add(key + ".");
                if (obj.size() == 0) {
                    map.put(concatPath(path), EMPTY_OBJECT);
                    path.remove(path.size() - 1);
                    continue;
                }
                parseObj(obj, path, map);
                path.remove(path.size() - 1);
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                path.add(key);
                if (jsonArray.size() == 0) {
                    map.put(concatPath(path), EMPTY_ARRAY);
                    path.remove(path.size() - 1);
                    continue;
                }
                parseArr(jsonArray, path, map);
                path.remove(path.size() - 1);
            } else {
                path.add(key);
                map.put(concatPath(path), value.toString());
                path.remove(path.size() - 1);
            }
        }
    }

    private static void parseArr(JSONArray jsonArr, List<String> path, Map<String, String> map) {
        for (int i = 0; i < jsonArr.size(); i++) {
            Object val = jsonArr.get(i);
            if (val instanceof JSONObject) {
                path.add("[" + i + "].");
                parseObj((JSONObject) val, path, map);
                path.remove(path.size() - 1);
            } else if (val instanceof JSONArray) {
                parseArr((JSONArray) jsonArr.get(i), path, map);
            } else {
                path.add("[" + i + "]");
                map.put(concatPath(path), val.toString());
                path.remove(path.size() - 1);
            }
        }
    }

    private static String concatPath(List<String> paths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.size() - 1; i++) {
            sb.append(paths.get(i));
        }
        String last = paths.get(paths.size() - 1);
        if (last.endsWith(".")) {
            last = last.substring(0, last.length() - 1);
        }
        sb.append(last);
        return sb.toString();
    }

}
