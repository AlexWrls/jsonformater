package ru.taa.jsonformater.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.utils.FormatUtils;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataTableToJsonService {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");
    private static final String ROOT = "{}";
    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат DATA_TABLE";
    private static final String EXCEPT_ARRAY_IDX = "Нарушен порядок добавления в массив %s, ожидаемый индекс:%d заданный индекс:%d";
    private static final String EMPTY_ARRAY = "[]";
    private static final String EMPTY_OBJECT = "{}";

    public ObjectRs format(String table) {
        try {
            String resultJson = bind(ROOT, FormatUtils.prepareData(table));
            return ObjectRs.builder().txt(resultJson).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }
    }

    private String bind(String content, Map<String, String> params) throws ParseException {
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(content);
        params.forEach((key, val) -> {
            String[] keys = key.split("\\.");
            int idx = 0;
            parseObj(jsonObject, keys, idx, val);
        });
        return jsonObject.toJSONString();
    }

    private void parseObj(JSONObject jsonObj, String[] keys, int idx, String val) {
        String key = keys[idx];
        Matcher matcher = ARRAY_PATTERN.matcher(key);
        if (matcher.find()) {
            key = matcher.group(1);
            int i = Integer.parseInt(matcher.group(2));
            JSONArray array = (JSONArray) jsonObj.get(key);
            if (Objects.isNull(array)) {
                jsonObj.put(key, new JSONArray());
                array = (JSONArray) jsonObj.get(key);
            }
            if (keys.length - 1 == idx) {
                setValue(array, val, i);
                return;
            }
            Object value = getArrValue(array, i);
            parseObj((JSONObject) value, keys, ++idx, val);
        } else {
            if (keys.length - 1 == idx) {
                setObjectValue(jsonObj, key, val);
                return;
            }
            Object value = getObjValue(jsonObj, key, val);
            parseObj((JSONObject) value, keys, ++idx, val);
        }
    }

    private Object getObjValue(JSONObject jsonObj, String key, String val) {
        Object object = jsonObj.get(key);
        if (Objects.isNull(object)) {
            jsonObj.put(key, new JSONObject());
            object = jsonObj.get(key);
        }
        return object;
    }

    private Object getArrValue(JSONArray array, int i) {
        if (i < array.size()) {
            return array.get(i);
        } else {
            array.add(new JSONObject());
            try {
                return array.get(i);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException(String.format(EXCEPT_ARRAY_IDX, array, array.size(), i));
            }
        }
    }

    private void setObjectValue(JSONObject jsonObj, String key, String val) {
        if (EMPTY_OBJECT.equals(val)) {
            jsonObj.put(key, new JSONObject());
        } else if (EMPTY_ARRAY.equals(val)) {
            jsonObj.put(key, new JSONArray());
        } else {
            jsonObj.put(key, val);
        }
    }

    private void setValue(JSONArray array, String val, int i) {
        if (i < array.size()) {
            array.set(i, val);
        } else if (i == array.size()) {
            array.add(i, val);
        } else {
            throw new RuntimeException(String.format(EXCEPT_ARRAY_IDX, array, array.size(), i));
        }
    }
}
