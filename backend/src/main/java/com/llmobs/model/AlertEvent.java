package com.llmobs.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "metric_value", precision = 14, scale = 4)
    private BigDecimal metricValue;

    @Column(nullable = false)
    private boolean notified;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
