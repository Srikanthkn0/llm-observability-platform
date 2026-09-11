package com.llmobs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class SpanIngestRequest {
    private UUID id;
    private UUID parentSpanId;
    @NotBlank
    private String name;
    private String kind = "LLM";
    private String status = "OK";
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
