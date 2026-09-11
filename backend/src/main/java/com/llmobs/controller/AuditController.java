package com.llmobs.controller;

import com.llmobs.dto.PageResponse;
import com.llmobs.exception.ApiException;
import com.llmobs.model.AuditLog;
import com.llmobs.model.Role;
import com.llmobs.repository.AuditLogRepository;
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
@RequestMapping("/api/v1/projects/{projectId}/audit-logs")
@RequiredArgsConstructor
public class AuditController {
    private final AuditLogRepository auditLogRepository;
    private final ProjectService projectService;

    @GetMapping
    public PageResponse<AuditLog> list(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        if (!(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT required");
        }
        var membership = projectService.membership(projectId, user.getId());
        if (membership.getRole() == Role.VIEWER && user.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Insufficient role for audit logs");
        }
        Page<AuditLog> result = auditLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(page, size));
        return PageResponse.<AuditLog>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }
}
