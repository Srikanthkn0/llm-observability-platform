package com.llmobs.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "spans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Span {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trace_id", nullable = false)
    private UUID traceId;

    @Column(name = "parent_span_id")
    private UUID parentSpanId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 64)
    private String kind;

    @Column(nullable = false, length = 32)
    private String status;

    private String model;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output_text", columnDefinition = "TEXT")
    private String outputText;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", columnDefinition = "jsonb")
    private Map<String, Object> attributesJson;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (kind == null) kind = "LLM";
        if (status == null) status = "OK";
    }
}
