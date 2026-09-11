package com.llmobs.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmobs.model.Span;
import com.llmobs.model.Trace;
import com.llmobs.model.TraceStatus;
import com.llmobs.repository.SpanRepository;
import com.llmobs.repository.TraceRepository;
import com.llmobs.service.GuardrailService;
import com.llmobs.service.PlatformMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TraceProcessorConsumer {
    private final ObjectMapper objectMapper;
    private final TraceRepository traceRepository;
    private final SpanRepository spanRepository;
    private final GuardrailService guardrailService;
    private final TraceEventPublisher publisher;
    private final PlatformMetrics platformMetrics;

    @KafkaListener(topics = "${llmobs.kafka.topics.traces-ingested}", groupId = "llmobs-trace-processor")
    @Transactional
    public void onIngested(String payload) {
        String key = "unknown";
        try {
            JsonNode node = objectMapper.readTree(payload);
            UUID traceId = UUID.fromString(node.get("traceId").asText());
            key = traceId.toString();
            Trace trace = traceRepository.findById(traceId).orElse(null);
            if (trace == null) {
                log.warn("Trace {} not found for processing", traceId);
                return;
            }

            List<Span> spans = spanRepository.findByTraceIdOrderByStartedAtAsc(traceId);
            String input = spans.stream().map(Span::getInputText).filter(t -> t != null).findFirst().orElse("");
            String output = spans.stream().map(Span::getOutputText).filter(t -> t != null)
                    .reduce((a, b) -> b).orElse("");

            GuardrailService.GuardrailResult result = guardrailService.evaluate(
                    trace.getProjectId(), traceId, input, output);

            if (result.blocked()) {
                platformMetrics.blocked();
                trace.setStatus(TraceStatus.BLOCKED);
                trace.setErrorMessage("Blocked by guardrail policy");
            } else {
                trace.setStatus(TraceStatus.PROCESSED);
            }
            trace.setProcessedAt(Instant.now());
            traceRepository.save(trace);

            publisher.publishProcessed(trace.getProjectId(), traceId, trace.getStatus().name());
            if (!result.blocked()) {
                publisher.publishEvalsRequested(trace.getProjectId(), traceId);
            }
            log.info("Processed trace {} status={}", traceId, trace.getStatus());
        } catch (Exception e) {
            log.error("Failed processing ingested trace: {}", e.getMessage(), e);
            publisher.publishDlq(key, payload, e.getMessage());
        }
    }
}
