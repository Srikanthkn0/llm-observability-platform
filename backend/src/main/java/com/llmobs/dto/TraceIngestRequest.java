package com.llmobs.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class TraceIngestRequest {
    @NotNull
    private UUID traceId;
    private String name;
    private String model;
    private String provider;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer latencyMs;
    private String userId;
    private String sessionId;
    private Map<String, Object> metadata;
    private Instant startedAt;
    private Instant endedAt;
    @Valid
    private List<SpanIngestRequest> spans = new ArrayList<>();
}
