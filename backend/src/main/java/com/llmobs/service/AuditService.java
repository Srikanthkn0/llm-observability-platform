package com.llmobs.service;

import com.llmobs.model.AuditLog;
import com.llmobs.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(UUID actorUserId, UUID projectId, String action, String resourceType,
                    String resourceId, Map<String, Object> details, String ip) {
        auditLogRepository.save(AuditLog.builder()
                .actorUserId(actorUserId)
                .projectId(projectId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .detailsJson(details)
                .ipAddress(ip)
                .build());
    }
}
