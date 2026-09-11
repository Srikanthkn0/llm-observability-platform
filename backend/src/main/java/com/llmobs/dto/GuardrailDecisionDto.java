package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class GuardrailDecisionDto {
    private UUID id;
    private UUID traceId;
    private String ruleName;
    private String category;
    private String action;
    private boolean matched;
    private String severity;
    private Map<String, Object> details;
    private Instant createdAt;
}
