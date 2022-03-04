package ru.taa.jsonformater.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.JsonRs;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataTableToJsonService {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("(.*)(\\[([0-9]+)])");
    private static final String PATH_SEPARATOR = ".";

    private static final String WRONG_LAST_INDEX_ERROR = "Неверно задан эелемент массива. Текущий порядковый номер: `%d`, ожидаемый: `%d`";
    private static final String PATH_BEGIN = "$";


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

    private String bind(String content, Map<String, String> params) {
        DocumentContext ctx = JsonPath.parse(content);
        params.forEach((k, v) -> modify(ctx, k, v));
        return ctx.jsonString();
    }

    /**
     * Установка нового значения в существующем пути или создание нового пути в json файле
     *
     * @param ctx   контекст json файла
     * @param path  новый путь
     * @param value значение для установки
     */
    private void modify(DocumentContext ctx, String path, String value) {
        String rootPath = PATH_BEGIN + PATH_SEPARATOR + path;
        if (hasPath(ctx, rootPath)) {
            deleteOrSet(ctx, path, value, rootPath);
            return;
        } else if (Objects.isNull(value)) {
            log.debug("New parameter value is null and path not found={}", path);
            return;
        }

        String[] splitPath = rootPath.split("\\.");
        StringBuilder newPath = new StringBuilder(PATH_BEGIN);

        for (int i = 1; i < splitPath.length; i++) {
            String currPathItem = splitPath[i];
            if (hasPath(ctx, newPath + PATH_SEPARATOR + currPathItem)) { // пропуск существующих элементов пути
                newPath.append(PATH_SEPARATOR).append(currPathItem);
                continue;
            }
            doModify(ctx, newPath, value, currPathItem, i == splitPath.length - 1);
        }
    }

    private void doModify(DocumentContext ctx, StringBuilder newPath, String value,
                          String currPath, boolean isLastItemInPath) {
        Matcher matcher = ARRAY_PATTERN.matcher(currPath);
        if (matcher.matches()) { //если текущий элемент пути является массивом
            String nextPath = newPath + PATH_SEPARATOR + matcher.group(1);
            if (!hasPath(ctx, nextPath)) { //проверим, что существует сам массив и если нет - создадим
                log.trace("Root tag of array isn't found, create path: {}.{}", newPath, matcher.group(1));
                ctx.put(newPath.toString(), matcher.group(1), new ArrayList<>());
            }

            //если текущий элемент пути последний, то это простой массив - добавляем элемент
            if (isLastItemInPath) {
                log.trace("Last element of path `{}`: add array element={}", nextPath, value);
                ctx.add(nextPath, value);
            } else { //иначе это массив объектов - добавляем в массив новый объект
                log.trace("Add object to array by path `{}`", nextPath);
                ctx.add(nextPath, new LinkedHashMap<>());
            }
            int lastIndex = ctx.read(nextPath + ".length()", Integer.class) - 1;
            int expectedIndex = Integer.parseInt(matcher.group(3));

            newPath.append(PATH_SEPARATOR).append(currPath);
        } else if (isLastItemInPath) { // текущий элемент пути последний - добавим или заменим значение поля
            log.trace("Create or update path `{}` by value={}", newPath, value);
            ctx.put(newPath.toString(), currPath, value);
        } else { // если не последний, то это новый объект - создадим мапу
            log.trace("Create or update path `{}` by empty object", newPath);
            ctx.put(newPath.toString(), currPath, new LinkedHashMap<>());
            newPath.append(PATH_SEPARATOR).append(currPath);
        }
    }

    private void deleteOrSet(DocumentContext ctx, String path, String value, String rootPath) {
        if (Objects.isNull(value)) {
            log.debug("New parameter value is null, remove path={}", path);
            ctx.delete(rootPath);
        } else {
            log.trace("Change existed json parameter for path={}: val={}", path, value);
            ctx.set(rootPath, value);
        }
    }

    /**
     * Проверка существования значения по указанному пути в json
     *
     * @param ctx  контекст json файла для поиска пути
     * @param path путь в json файле
     * @return false - не существует значения по указанному пути, true - значение по указанному пути установлено
     */
    private boolean hasPath(ReadContext ctx, String path) {
        try {
            ctx.read(path);
        } catch (PathNotFoundException e) {
            log.trace("Path does not exists: {}", path);
            return false;
        }
        return true;
    }
}
