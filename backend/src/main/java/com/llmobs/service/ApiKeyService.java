package com.llmobs.service;

import com.llmobs.dto.ApiKeyCreatedResponse;
import com.llmobs.dto.CreateApiKeyRequest;
import com.llmobs.exception.ApiException;
import com.llmobs.model.ApiKey;
import com.llmobs.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public ApiKeyCreatedResponse create(UUID projectId, UUID userId, CreateApiKeyRequest request) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String raw = "llmobs_" + HexFormat.of().formatHex(bytes);
        String prefix = raw.substring(0, 12);
        ApiKey saved = apiKeyRepository.save(ApiKey.builder()
                .projectId(projectId)
                .name(request.getName())
                .keyPrefix(prefix)
                .keyHash(passwordEncoder.encode(raw))
                .createdBy(userId)
                .build());
        auditService.log(userId, projectId, "API_KEY_CREATE", "api_key", saved.getId().toString(), null, null);
        return ApiKeyCreatedResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .keyPrefix(prefix)
                .rawKey(raw)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<ApiKey> list(UUID projectId) {
        return apiKeyRepository.findByProjectIdAndRevokedAtIsNull(projectId);
    }

    @Transactional
    public void revoke(UUID projectId, UUID keyId, UUID userId) {
        ApiKey key = apiKeyRepository.findByIdAndProjectId(keyId, projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "API key not found"));
        key.setRevokedAt(Instant.now());
        apiKeyRepository.save(key);
        auditService.log(userId, projectId, "API_KEY_REVOKE", "api_key", keyId.toString(), null, null);
    }
}
