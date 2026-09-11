package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardMetricsDto {
    private long tracesLast24h;
    private long blockedLast24h;
    private long failedLast24h;
    private long evalFailuresLast24h;
    private BigDecimal costLast24h;
    private double avgLatencyMs;
    private Double avgQualityScore;
    private Double avgSafetyScore;
    private long guardrailBlocksTotal;
}
