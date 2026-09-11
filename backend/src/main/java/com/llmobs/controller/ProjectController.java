package com.llmobs.controller;

import com.llmobs.exception.ApiException;
import com.llmobs.model.Project;
import com.llmobs.security.UserPrincipal;
import com.llmobs.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public List<Project> list(Authentication auth) {
        UserPrincipal user = requireUser(auth);
        return projectService.listForUser(user.getId());
    }

    @GetMapping("/{projectId}")
    public Project get(@PathVariable UUID projectId, Authentication auth) {
        UserPrincipal user = requireUser(auth);
        projectService.requireMembership(projectId, user.getId());
        return projectService.get(projectId);
    }

    private UserPrincipal requireUser(Authentication auth) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        return user;
    }
}
