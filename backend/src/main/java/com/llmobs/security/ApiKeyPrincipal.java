package com.llmobs.security;

import lombok.Getter;
import java.util.UUID;

@Getter
public class ApiKeyPrincipal {
    private final UUID apiKeyId;
    private final UUID projectId;
    private final String keyPrefix;

    public ApiKeyPrincipal(UUID apiKeyId, UUID projectId, String keyPrefix) {
        this.apiKeyId = apiKeyId;
        this.projectId = projectId;
        this.keyPrefix = keyPrefix;
    }
}
