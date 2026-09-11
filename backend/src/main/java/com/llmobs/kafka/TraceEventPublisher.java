package com.llmobs.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TraceEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llmobs.kafka.topics.traces-ingested}")
    private String tracesIngested;
    @Value("${llmobs.kafka.topics.traces-processed}")
    private String tracesProcessed;
    @Value("${llmobs.kafka.topics.traces-dlq}")
    private String tracesDlq;
    @Value("${llmobs.kafka.topics.evals-requested}")
    private String evalsRequested;

    public void publishIngested(UUID projectId, UUID traceId) {
        send(tracesIngested, traceId.toString(), Map.of(
                "event", "traces.ingested",
                "projectId", projectId.toString(),
                "traceId", traceId.toString()
        ));
    }

    public void publishProcessed(UUID projectId, UUID traceId, String status) {
        send(tracesProcessed, traceId.toString(), Map.of(
                "event", "traces.processed",
                "projectId", projectId.toString(),
                "traceId", traceId.toString(),
                "status", status
        ));
    }

    public void publishEvalsRequested(UUID projectId, UUID traceId) {
        send(evalsRequested, traceId.toString(), Map.of(
                "event", "evals.requested",
                "projectId", projectId.toString(),
                "traceId", traceId.toString()
        ));
    }

    public void publishDlq(String key, String raw, String error) {
        send(tracesDlq, key, Map.of(
                "event", "traces.dlq",
                "payload", raw == null ? "" : raw,
                "error", error == null ? "" : error
        ));
    }

    private void send(String topic, String key, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
            log.debug("Published to {}: {}", topic, key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Kafka payload for topic {}", topic, e);
        }
    }
}
