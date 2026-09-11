package com.llmobs.controller;

import com.llmobs.dto.GuardrailDecisionDto;
import com.llmobs.dto.PageResponse;
import com.llmobs.exception.ApiException;
import com.llmobs.model.GuardrailDecision;
import com.llmobs.repository.GuardrailDecisionRepository;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/guardrails")
@RequiredArgsConstructor
public class GuardrailController {
    private final GuardrailDecisionRepository guardrailDecisionRepository;
    private final ProjectService projectService;

    @GetMapping
    public PageResponse<GuardrailDecisionDto> list(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        projectService.requireMembership(projectId, user.getId());
        Page<GuardrailDecision> result = guardrailDecisionRepository.findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(page, size));
        return PageResponse.<GuardrailDecisionDto>builder()
                .content(result.getContent().stream().map(g -> GuardrailDecisionDto.builder()
                        .id(g.getId())
                        .traceId(g.getTraceId())
                        .ruleName(g.getRuleName())
                        .category(g.getCategory())
                        .action(g.getAction())
                        .matched(g.isMatched())
                        .severity(g.getSeverity())
                        .details(g.getDetailsJson())
                        .createdAt(g.getCreatedAt())
                        .build()).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }
}
