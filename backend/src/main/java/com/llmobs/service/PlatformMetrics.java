package com.llmobs.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PlatformMetrics {
    private final Counter tracesIngested;
    private final Counter tracesDuplicate;
    private final Counter tracesBlocked;

    public PlatformMetrics(MeterRegistry registry) {
        this.tracesIngested = Counter.builder("llmobs.traces.ingested").description("Traces accepted").register(registry);
        this.tracesDuplicate = Counter.builder("llmobs.traces.duplicate").description("Idempotent duplicates").register(registry);
        this.tracesBlocked = Counter.builder("llmobs.traces.blocked").description("Traces blocked by guardrails").register(registry);
    }

    public void ingested() { tracesIngested.increment(); }
    public void duplicate() { tracesDuplicate.increment(); }
    public void blocked() { tracesBlocked.increment(); }
}
