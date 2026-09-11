package com.llmobs.controller;

import com.llmobs.dto.DashboardMetricsDto;
import com.llmobs.exception.ApiException;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.MetricsService;
import com.llmobs.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/metrics")
@RequiredArgsConstructor
public class MetricsController {
    private final MetricsService metricsService;
    private final ProjectService projectService;

    @GetMapping("/dashboard")
    public DashboardMetricsDto dashboard(@PathVariable UUID projectId, Authentication auth) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        projectService.requireMembership(projectId, user.getId());
        return metricsService.dashboard(projectId);
    }
}
