package com.llmobs.controller;

import com.llmobs.dto.CreateAlertRuleRequest;
import com.llmobs.dto.PageResponse;
import com.llmobs.exception.ApiException;
import com.llmobs.model.AlertEvent;
import com.llmobs.model.AlertRule;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.AlertService;
import com.llmobs.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;
    private final ProjectService projectService;

    @GetMapping("/rules")
    public List<AlertRule> rules(@PathVariable UUID projectId, Authentication auth) {
        require(auth, projectId);
        return alertService.listRules(projectId);
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public AlertRule createRule(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateAlertRuleRequest request,
            Authentication auth) {
        require(auth, projectId);
        return alertService.create(projectId, request);
    }

    @GetMapping("/events")
    public PageResponse<AlertEvent> events(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        require(auth, projectId);
        Page<AlertEvent> result = alertService.listEvents(projectId, page, size);
        return PageResponse.<AlertEvent>builder()
                .content(result.getContent())
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
