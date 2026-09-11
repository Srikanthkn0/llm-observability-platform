package com.llmobs.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmobs.model.Trace;
import com.llmobs.repository.TraceRepository;
import com.llmobs.service.EvalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvalProcessorConsumer {
    private final ObjectMapper objectMapper;
    private final TraceRepository traceRepository;
    private final EvalService evalService;
    private final TraceEventPublisher publisher;

    @KafkaListener(topics = "${llmobs.kafka.topics.evals-requested}", groupId = "llmobs-eval-processor")
    @Transactional
    public void onEvalsRequested(String payload) {
        String key = "unknown";
        try {
            JsonNode node = objectMapper.readTree(payload);
            UUID traceId = UUID.fromString(node.get("traceId").asText());
            key = traceId.toString();
            Trace trace = traceRepository.findById(traceId).orElse(null);
            if (trace == null) {
                log.warn("Trace {} missing for eval", traceId);
                return;
            }
            var evals = evalService.evaluateTrace(trace);
            log.info("Wrote {} evaluations for trace {}", evals.size(), traceId);
        } catch (Exception e) {
            log.error("Eval processing failed: {}", e.getMessage(), e);
            publisher.publishDlq(key, payload, "eval:" + e.getMessage());
        }
    }
}
