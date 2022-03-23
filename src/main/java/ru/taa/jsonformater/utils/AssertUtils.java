package ru.taa.jsonformater.utils;

import lombok.experimental.UtilityClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AssertUtils {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.*)\\[(\\d+)]$");

    /**
     * Валидация ответа по ресту по требуемым параметрам
     */
    public static void assertJsonByParam(String message, Map<String, String> param) {
        try {
            String normalizeJson = FormatUtils.normalizeJson(message);
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(normalizeJson);
            param.forEach((k, v) -> {
                String[] split = k.split("\\.");
                jsonParseToParam(jsonObject, split, v, 0);
            });
        } catch (ParseException e) {
            throw new RuntimeException("Ошибка разбора json.\n");
        }
    }

    /**
     * Парсинг json и валидация значений по параметрам
     *
     * @param jsonObject объект json
     * @param paths      путь параметров
     * @param chekValue  проверяемое значение
     * @param index      индекс параметра json
     */
    private static void jsonParseToParam(JSONObject jsonObject, String[] paths, String chekValue, int index) {
        String path = paths[index];
        if (paths.length - 1 == index) {
            String value = String.valueOf(jsonObject.get(path));
            Matcher matcher = ARRAY_PATTERN.matcher(path);
            if (matcher.find()) {
                path = matcher.group(1);
                int arrIndex = Integer.parseInt(matcher.group(2));
                JSONArray arr = (JSONArray) jsonObject.get(path);
                value = String.valueOf(arr.get(arrIndex));
            }
            String messageEx = String.format("\nАктуальный:<%s>  Ожидаемый:<%s>\nСтрока:\n%s\n", value, chekValue, getTextException(paths, chekValue));
            Assert.assertEquals(messageEx, value, chekValue.trim());
            return;
        }
        try {
            index++;
            Matcher matcher = ARRAY_PATTERN.matcher(path);
            if (matcher.find()) {
                path = matcher.group(1);
                int arrIndex = Integer.parseInt(matcher.group(2));
                JSONArray arr = (JSONArray) jsonObject.get(path);
                Object value = arr.get(arrIndex);
                jsonParseToParam((JSONObject) value, paths, chekValue, index);
            } else {
                Object value = jsonObject.get(path);
                jsonParseToParam((JSONObject) value, paths, chekValue, index);
            }
        } catch (Exception e) {
            throw new RuntimeException("\nОшибка разбора json, указанный путь не существует.\nСтрока:\n" + getTextException(paths, chekValue));
        }
    }

    /**
     * Формирование строки печати ошибки
     */
    private String getTextException(String[] paths, String chekValue) {
        StringBuilder sb = new StringBuilder("| [");
        Arrays.stream(paths).forEach(i -> sb.append(i).append("."));
        sb.append("] | ").append(chekValue).append(" |");
        return sb.toString();
    }
}
