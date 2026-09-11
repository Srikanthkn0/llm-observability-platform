package com.llmobs.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 64)
    private String metric;

    @Column(nullable = false, length = 8)
    private String operator;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal threshold;

    @Column(name = "window_minutes", nullable = false)
    private Integer windowMinutes;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "slack_webhook")
    private String slackWebhook;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (windowMinutes == null) windowMinutes = 15;
    }
}
