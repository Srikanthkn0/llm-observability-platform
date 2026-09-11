package com.llmobs.controller;

import com.llmobs.dto.EvaluationDto;
import com.llmobs.dto.PageResponse;
import com.llmobs.exception.ApiException;
import com.llmobs.model.Evaluation;
import com.llmobs.repository.EvaluationRepository;
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
@RequestMapping("/api/v1/projects/{projectId}/evaluations")
@RequiredArgsConstructor
public class EvaluationController {
    private final EvaluationRepository evaluationRepository;
    private final ProjectService projectService;

    @GetMapping
    public PageResponse<EvaluationDto> list(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        require(auth, projectId);
        Page<Evaluation> result = evaluationRepository.findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(page, size));
        return PageResponse.<EvaluationDto>builder()
                .content(result.getContent().stream().map(e -> EvaluationDto.builder()
                        .id(e.getId())
                        .traceId(e.getTraceId())
                        .evalType(e.getEvalType())
                        .score(e.getScore())
                        .passed(e.getPassed())
                        .judge(e.getJudge())
                        .rationale(e.getRationale())
                        .details(e.getDetailsJson())
                        .createdAt(e.getCreatedAt())
                        .build()).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private void require(Authentication auth, UUID projectId) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        projectService.requireMembership(projectId, user.getId());
    }
}
