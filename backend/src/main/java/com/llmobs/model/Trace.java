package com.llmobs.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "traces")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trace {
    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TraceStatus status;

    private String model;
    private String provider;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "estimated_cost_usd", precision = 12, scale = 6)
    private BigDecimal estimatedCostUsd;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "user_id_ext")
    private String userIdExt;

    @Column(name = "session_id")
    private String sessionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    void prePersist() {
        if (ingestedAt == null) ingestedAt = Instant.now();
        if (status == null) status = TraceStatus.PENDING;
        if (inputTokens == null) inputTokens = 0;
        if (outputTokens == null) outputTokens = 0;
        if (totalTokens == null) totalTokens = 0;
        if (estimatedCostUsd == null) estimatedCostUsd = BigDecimal.ZERO;
    }
}
