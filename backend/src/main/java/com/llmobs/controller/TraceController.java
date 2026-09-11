package com.llmobs.controller;

import com.llmobs.dto.*;
import com.llmobs.exception.ApiException;
import com.llmobs.model.TraceStatus;
import com.llmobs.security.ApiKeyPrincipal;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.ProjectService;
import com.llmobs.service.TraceIngestService;
import com.llmobs.service.TraceQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TraceController {
    private final TraceIngestService ingestService;
    private final TraceQueryService queryService;
    private final ProjectService projectService;

    @PostMapping("/traces")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TraceIngestResponse ingest(@Valid @RequestBody TraceIngestRequest request, Authentication auth) {
        if (!(auth.getPrincipal() instanceof ApiKeyPrincipal apiKey)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "API key required for ingest");
        }
        return ingestService.ingest(apiKey, request);
    }

    @GetMapping("/projects/{projectId}/traces")
    public PageResponse<TraceSummaryDto> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TraceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        requireUserProject(auth, projectId);
        return queryService.list(projectId, status, page, size);
    }

    @GetMapping("/projects/{projectId}/traces/{traceId}")
    public TraceDetailDto detail(
            @PathVariable UUID projectId,
            @PathVariable UUID traceId,
            Authentication auth) {
        requireUserProject(auth, projectId);
        return queryService.getDetail(projectId, traceId);
    }

    private void requireUserProject(Authentication auth, UUID projectId) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        projectService.requireMembership(projectId, user.getId());
    }
}
