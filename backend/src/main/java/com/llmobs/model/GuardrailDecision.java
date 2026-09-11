package com.llmobs.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "guardrail_decisions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuardrailDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trace_id")
    private UUID traceId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(nullable = false)
    private boolean matched;

    @Column(nullable = false, length = 32)
    private String severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "jsonb")
    private Map<String, Object> detailsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (severity == null) severity = "INFO";
    }
}
