package ru.taa.jsonformater.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.JsonRs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataTableToJsonService {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");


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
    private static final String RES2 = "{}";

    public static void main(String[] args) {
        Map<String, String> map = new LinkedHashMap<>();
//        map.put("persons[0].bio.birthDay", "1111111");
//        map.put("persons[0]", "22222");
//        map.put("persons[3].3333.4444", "5555");
        map.put("name[0].firs", "Ivan");
        map.put("name[0].last", "Petrov");
        map.put("jjjj[0]", "");
        final String bind = bind(RES2, map);
        final String s = bind.replaceAll("\\[\"\"]", "[]");
        System.out.println(s);
    }

    @SneakyThrows
    private static String bind(String content, Map<String, String> params) {
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(content);

        params.forEach((key, val) -> {
            String[] keys = key.split("\\.");
            int idx = 0;
            parseObj(jsonObject, keys, idx, val);
        });
        return jsonObject.toJSONString();
    }

    private static void parseObj(JSONObject jsonObj, String[] keys, int idx, String val) {
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
            if (setValue(keys, idx, array, val, i)) {
                return;
            }
            Object value = getArrValue(array, i);
            parseObj((JSONObject) value, keys, ++idx, val);
        } else {
            if (keys.length - 1 == idx) {
                jsonObj.put(key, val);
                return;
            }
            Object value = getObjValue(jsonObj, key, val);
            parseObj((JSONObject) value, keys, ++idx, val);
        }
    }

    private static Object getObjValue(JSONObject jsonObj, String key, String val) {
        Object object = jsonObj.get(key);
        if (Objects.isNull(object)) {
            jsonObj.put(key, new JSONObject());
            object = jsonObj.get(key);
        }
        return object;
    }

    private static Object getArrValue(JSONArray array, int i) {
        Object value;
        try {
            value = array.get(i);
        } catch (IndexOutOfBoundsException e) {
            array.add(new JSONObject());
            value = array.get(i);
        }
        return value;
    }

    private static boolean setValue(String[] keys, int idx, JSONArray array, String val, int i) {
        try {
            if (keys.length - 1 == idx) {
                array.set(i, val);
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            array.add(i, val);
            return true;
        }
        return false;
    }

    public JsonRs format(String table) {
        try {
            String resultJson = bind("{}", prepareData(table));
            return JsonRs.builder().jsonData(resultJson).build();
        } catch (Exception e) {
            return JsonRs.builder().jsonData("Ошибка разбора проверьте данные").build();
        }
    }

    public Map<String, String> prepareData(String data) {
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
}
