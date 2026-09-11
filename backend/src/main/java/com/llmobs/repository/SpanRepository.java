package com.llmobs.repository;

import com.llmobs.model.Span;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpanRepository extends JpaRepository<Span, UUID> {
    List<Span> findByTraceIdOrderByStartedAtAsc(UUID traceId);
}
