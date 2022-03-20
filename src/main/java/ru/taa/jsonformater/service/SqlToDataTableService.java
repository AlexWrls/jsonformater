package ru.taa.jsonformater.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.ObjectRs;
import ru.taa.jsonformater.utils.FormatUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SqlToDataTableService {

    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат QUERY_SQL";

    private static final Map<String, String> STRING_MAP = ImmutableMap.<String, String>builder()
            .put("\\n", "")
            .put("(\\s)+", " ")
            .build();

    public ObjectRs convert(String query, boolean onlyNotNull) {
        try {
            String result = tableSql(query, onlyNotNull);
            return ObjectRs.builder().txt(result).build();
        } catch (Exception e) {
            return ObjectRs.builder().txt(EXCEPT).build();
        }
    }

    private static String tableSql(String query, boolean onlyNotNull) {
        for (Map.Entry<String, String> entry : STRING_MAP.entrySet()) {
            query = query.replaceAll(entry.getKey(), entry.getValue());
        }
        Map<String, String> columns = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder(query);
        final int index = sb.indexOf("(");
        String tableName = query.substring(13, index).trim();
        if (tableName.contains(".")) {
            tableName = tableName.split("\\.")[1];
        }
        String[] str = query.substring(index + 1, query.lastIndexOf(")")).split(",");
        final List<String> collect = Arrays.stream(str)
                .map(String::trim)
                .collect(Collectors.toList());

        for (String value : collect) {
            boolean notNull = false;
            if (value.contains("NOT NULL") || value.contains("not null")) {
                notNull = true;
            }
            String[] s = value.split(" ");
            if (!onlyNotNull) {
                String columnName = s[0];
                String type = notNull ? s[1] + "*" : s[1];
                columns.put(columnName, type);
            } else {
                if (notNull) {
                    columns.put(s[0], s[1] + "*");
                }
            }
        }
        StringBuilder res = new StringBuilder("Допустим существует таблица " + tableName + " c параметрами:\n");
        columns.forEach((k, v) -> res.append("| ").append(k));
        res.append("|\n");
        columns.forEach((k, v) -> res.append("| ").append(v));
        res.append("|");
        return res.toString();
    }
}
