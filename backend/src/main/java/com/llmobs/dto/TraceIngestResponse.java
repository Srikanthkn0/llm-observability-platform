package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class TraceIngestResponse {
    private UUID traceId;
    private String status;
    private boolean duplicate;
    private String message;
}
