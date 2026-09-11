package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class SpanDto {
    private UUID id;
    private UUID parentSpanId;
    private String name;
    private String kind;
    private String status;
    private String model;
    private String inputText;
    private String outputText;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer latencyMs;
    private Map<String, Object> attributes;
    private Instant startedAt;
    private Instant endedAt;
}
