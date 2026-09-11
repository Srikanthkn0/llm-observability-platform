package com.llmobs.dto;

import com.llmobs.model.TraceStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TraceSummaryDto {
    private UUID id;
    private String name;
    private TraceStatus status;
    private String model;
    private String provider;
    private Integer totalTokens;
    private BigDecimal estimatedCostUsd;
    private Integer latencyMs;
    private Instant ingestedAt;
}
