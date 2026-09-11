package com.llmobs.repository;

import com.llmobs.model.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
    List<Evaluation> findByTraceId(UUID traceId);
    Page<Evaluation> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.projectId = :projectId AND e.evalType = :type AND e.createdAt >= :after")
    Double avgScoreByTypeSince(@Param("projectId") UUID projectId, @Param("type") String type, @Param("after") Instant after);

    long countByProjectIdAndPassedFalseAndCreatedAtAfter(UUID projectId, Instant after);
}
