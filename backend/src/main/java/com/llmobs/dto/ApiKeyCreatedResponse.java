package com.llmobs.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ApiKeyCreatedResponse {
    private UUID id;
    private String name;
    private String keyPrefix;
    private String rawKey;
    private Instant createdAt;
}
