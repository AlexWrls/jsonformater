package ru.taa.jsonformater.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QueryRs {
    private final String tableName;
    private final List<String> columns;
    private final List<String> values;
}
