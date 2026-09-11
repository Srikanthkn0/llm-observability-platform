package com.llmobs.repository;

import com.llmobs.model.GuardrailDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GuardrailDecisionRepository extends JpaRepository<GuardrailDecision, UUID> {
    List<GuardrailDecision> findByTraceId(UUID traceId);
    Page<GuardrailDecision> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
    long countByProjectIdAndActionAndMatchedTrue(UUID projectId, String action);
}
