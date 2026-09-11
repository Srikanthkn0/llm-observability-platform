package com.llmobs.controller;

import com.llmobs.dto.ApiKeyCreatedResponse;
import com.llmobs.dto.CreateApiKeyRequest;
import com.llmobs.exception.ApiException;
import com.llmobs.model.ApiKey;
import com.llmobs.model.Role;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.ApiKeyService;
import com.llmobs.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final ProjectService projectService;

    @GetMapping
    public List<Map<String, Object>> list(@PathVariable UUID projectId, Authentication auth) {
        UserPrincipal user = requireDev(auth, projectId);
        return apiKeyService.list(projectId).stream().map(k -> Map.<String, Object>of(
                "id", k.getId(),
                "name", k.getName(),
                "keyPrefix", k.getKeyPrefix(),
                "createdAt", k.getCreatedAt(),
                "lastUsedAt", k.getLastUsedAt() == null ? "" : k.getLastUsedAt()
        )).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyCreatedResponse create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateApiKeyRequest request,
            Authentication auth) {
        UserPrincipal user = requireDev(auth, projectId);
        return apiKeyService.create(projectId, user.getId(), request);
    }

    @DeleteMapping("/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID projectId, @PathVariable UUID keyId, Authentication auth) {
        UserPrincipal user = requireDev(auth, projectId);
        apiKeyService.revoke(projectId, keyId, user.getId());
    }

    private UserPrincipal requireDev(Authentication auth, UUID projectId) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        var membership = projectService.membership(projectId, user.getId());
        if (membership.getRole() == Role.VIEWER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Viewers cannot manage API keys");
        }
        return user;
    }
}
