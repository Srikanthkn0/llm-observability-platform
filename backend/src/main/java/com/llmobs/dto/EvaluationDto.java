package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class EvaluationDto {
    private UUID id;
    private UUID traceId;
    private String evalType;
    private BigDecimal score;
    private Boolean passed;
    private String judge;
    private String rationale;
    private Map<String, Object> details;
    private Instant createdAt;
}
