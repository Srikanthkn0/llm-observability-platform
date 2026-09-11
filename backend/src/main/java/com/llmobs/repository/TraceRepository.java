package com.llmobs.repository;

import com.llmobs.model.Trace;
import com.llmobs.model.TraceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface TraceRepository extends JpaRepository<Trace, UUID> {
    Page<Trace> findByProjectIdOrderByIngestedAtDesc(UUID projectId, Pageable pageable);

    Page<Trace> findByProjectIdAndStatusOrderByIngestedAtDesc(UUID projectId, TraceStatus status, Pageable pageable);

    long countByProjectIdAndIngestedAtAfter(UUID projectId, Instant after);

    @Query("SELECT COALESCE(SUM(t.estimatedCostUsd), 0) FROM Trace t WHERE t.projectId = :projectId AND t.ingestedAt >= :after")
    BigDecimal sumCostSince(@Param("projectId") UUID projectId, @Param("after") Instant after);

    @Query("SELECT COALESCE(AVG(t.latencyMs), 0) FROM Trace t WHERE t.projectId = :projectId AND t.ingestedAt >= :after AND t.latencyMs IS NOT NULL")
    Double avgLatencySince(@Param("projectId") UUID projectId, @Param("after") Instant after);

    @Query("SELECT COUNT(t) FROM Trace t WHERE t.projectId = :projectId AND t.status = 'BLOCKED' AND t.ingestedAt >= :after")
    long countBlockedSince(@Param("projectId") UUID projectId, @Param("after") Instant after);

    @Query("SELECT COUNT(t) FROM Trace t WHERE t.projectId = :projectId AND t.status = 'FAILED' AND t.ingestedAt >= :after")
    long countFailedSince(@Param("projectId") UUID projectId, @Param("after") Instant after);
}
