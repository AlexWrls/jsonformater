package ru.taa.jsonformater.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import ru.taa.jsonformater.dto.QueryRs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SqlToDataTableService {
    private static final String EXCEPT = "Ошибка конвертации, данные должны иметь формат QUERY_SQL";


    public QueryRs convert(String query, boolean onlyNotNull) {
        try {
            QueryRs result = tableSql(query, onlyNotNull);
            return result;
        } catch (Exception e) {
            return QueryRs.builder().tableName(EXCEPT).build();
        }
    }

    /**
     * Получить объект с query запроса
     *
     * @param query       sql запрос на создание таблицы
     * @param onlyNotNull только поля not null
     * @return объект с названием и параметрми таблицы
     */
    private static QueryRs tableSql(String query, boolean onlyNotNull) {
        for (Map.Entry<String, String> entry : STRING_MAP.entrySet()) {
            query = query.replaceAll(entry.getKey(), entry.getValue());
        }
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder(query);
        final int index = sb.indexOf("(");
        String tableName = query.substring(13, index).trim();
        if (tableName.contains(".")) {
            tableName = tableName.split("\\.")[1];
        }
        int i1 = query.lastIndexOf(")");
        int i2 = query.lastIndexOf("constraint");
        int cut = i2 != -1 ? Math.min(i1, i2) : i1;
        String[] str = query.substring(index + 1, cut).split(",");
        final List<String> collect = Arrays.stream(str)
                .map(String::trim)
                .filter(i -> !i.contains("$") && !i.isEmpty())
                .collect(Collectors.toList());
        for (String value : collect) {
            boolean notNull = value.contains("NOT NULL") || value.contains("not null");
            String[] s = value.split(" ");
            if (!onlyNotNull) {
                String columnName = s[0];
                String type = notNull ? s[1] + "*" : s[1];
                columns.add(columnName);
                values.add(type);
            } else {
                if (notNull) {
                    columns.add(s[0]);
                    values.add(s[1] + "*");
                }
            }
        }
        return QueryRs.builder().tableName(tableName).columns(columns).values(values).build();
    }

    private static final Map<String, String> STRING_MAP = ImmutableMap.<String, String>builder()
            .put("\\n", "")
            .put("(\\s)+", " ")
            .build();
}
