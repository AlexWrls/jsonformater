package ru.taa.jsonformater.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JsonRs {
    private final String jsonData;
}
