package com.llmobs.dto;

import com.llmobs.model.TraceStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class TraceDetailDto {
    private UUID id;
    private UUID projectId;
    private String name;
    private TraceStatus status;
    private String model;
    private String provider;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private BigDecimal estimatedCostUsd;
    private Integer latencyMs;
    private String userIdExt;
    private String sessionId;
    private Map<String, Object> metadata;
    private Instant startedAt;
    private Instant endedAt;
    private Instant ingestedAt;
    private Instant processedAt;
    private String errorMessage;
    private List<SpanDto> spans;
    private List<EvaluationDto> evaluations;
    private List<GuardrailDecisionDto> guardrails;
}
