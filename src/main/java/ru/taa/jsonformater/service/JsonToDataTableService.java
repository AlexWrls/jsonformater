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

    /**
     * Разбор json объекта с корневого jsonObj для получения параметров params (путь и значение))
     * используется рекурсивный обход дерева элементов
     *
     * @param jsonObj элемент json для разбора
     * @param path    список для формирования пути
     * @param params  список парметров разобранных элемнтов xml (путь и значение)
     */
    private static void parseObj(JSONObject jsonObj, List<String> path, Map<String, String> params) {
        Set<String> keys = jsonObj.keySet();
        for (String key : keys) {
            Object value = jsonObj.get(key);
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                path.add(key + ".");
                if (obj.size() == 0) {
                    params.put(concatPath(path), EMPTY_OBJECT);
                    path.remove(path.size() - 1);
                    continue;
                }
                parseObj(obj, path, params);
                path.remove(path.size() - 1);
            } else if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                path.add(key);
                if (jsonArray.size() == 0) {
                    params.put(concatPath(path), EMPTY_ARRAY);
                    path.remove(path.size() - 1);
                    continue;
                }
                parseArr(jsonArray, path, params);
                path.remove(path.size() - 1);
            } else {
                path.add(key);
                params.put(concatPath(path), value.toString());
                path.remove(path.size() - 1);
            }
        }
    }

    /**
     * Разбор json массива jsonArr для получения параметров params (путь и значение))
     * используется рекурсивный обход дерева элементов
     *
     * @param jsonArr массив json для разбора
     * @param path    список для формирования пути
     * @param params  список парметров разобранных элемнтов xml (путь и значение)
     */
    private static void parseArr(JSONArray jsonArr, List<String> path, Map<String, String> params) {
        for (int i = 0; i < jsonArr.size(); i++) {
            Object val = jsonArr.get(i);
            if (val instanceof JSONObject) {
                path.add("[" + i + "].");
                parseObj((JSONObject) val, path, params);
                path.remove(path.size() - 1);
            } else if (val instanceof JSONArray) {
                parseArr((JSONArray) jsonArr.get(i), path, params);
            } else {
                path.add("[" + i + "]");
                params.put(concatPath(path), val.toString());
                path.remove(path.size() - 1);
            }
        }
    }

    /**
     * Объединяет список пути в строку с разделением '.'
     *
     * @param paths список с элементами названия пути
     * @return строка объединненого списка
     */
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
