package com.llmobs.service;

import com.llmobs.dto.SpanIngestRequest;
import com.llmobs.dto.TraceIngestRequest;
import com.llmobs.dto.TraceIngestResponse;
import com.llmobs.exception.ApiException;
import com.llmobs.kafka.TraceEventPublisher;
import com.llmobs.model.Span;
import com.llmobs.model.Trace;
import com.llmobs.model.TraceStatus;
import com.llmobs.repository.SpanRepository;
import com.llmobs.repository.TraceRepository;
import com.llmobs.security.ApiKeyPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraceIngestService {
    private final TraceRepository traceRepository;
    private final SpanRepository spanRepository;
    private final RateLimitService rateLimitService;
    private final CostEstimator costEstimator;
    private final TraceEventPublisher publisher;
    private final PlatformMetrics platformMetrics;

    @Transactional
    public TraceIngestResponse ingest(ApiKeyPrincipal apiKey, TraceIngestRequest request) {
        UUID projectId = apiKey.getProjectId();
        rateLimitService.checkTraceIngest(projectId);

        if (traceRepository.existsById(request.getTraceId())) {
            platformMetrics.duplicate();
            return TraceIngestResponse.builder()
                    .traceId(request.getTraceId())
                    .status("DUPLICATE")
                    .duplicate(true)
                    .message("Trace already ingested (idempotent)")
                    .build();
        }

        int inputTokens = request.getInputTokens() != null ? request.getInputTokens() : sumTokens(request, true);
        int outputTokens = request.getOutputTokens() != null ? request.getOutputTokens() : sumTokens(request, false);

        Trace trace = Trace.builder()
                .id(request.getTraceId())
                .projectId(projectId)
                .name(request.getName() != null ? request.getName() : "unnamed")
                .status(TraceStatus.PENDING)
                .model(request.getModel())
                .provider(request.getProvider())
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .estimatedCostUsd(costEstimator.estimate(request.getModel(), inputTokens, outputTokens))
                .latencyMs(request.getLatencyMs())
                .userIdExt(request.getUserId())
                .sessionId(request.getSessionId())
                .metadataJson(request.getMetadata())
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .build();

        traceRepository.save(trace);

        if (request.getSpans() != null) {
            for (SpanIngestRequest s : request.getSpans()) {
                spanRepository.save(Span.builder()
                        .id(s.getId())
                        .traceId(trace.getId())
                        .parentSpanId(s.getParentSpanId())
                        .name(s.getName())
                        .kind(s.getKind() != null ? s.getKind() : "LLM")
                        .status(s.getStatus() != null ? s.getStatus() : "OK")
                        .model(s.getModel())
                        .inputText(s.getInputText())
                        .outputText(s.getOutputText())
                        .inputTokens(s.getInputTokens())
                        .outputTokens(s.getOutputTokens())
                        .latencyMs(s.getLatencyMs())
                        .attributesJson(s.getAttributes())
                        .startedAt(s.getStartedAt())
                        .endedAt(s.getEndedAt())
                        .build());
            }
        }

        publisher.publishIngested(projectId, trace.getId());
        platformMetrics.ingested();

        return TraceIngestResponse.builder()
                .traceId(trace.getId())
                .status("PENDING")
                .duplicate(false)
                .message("Trace accepted for processing")
                .build();
    }

    private int sumTokens(TraceIngestRequest request, boolean input) {
        if (request.getSpans() == null) return 0;
        return request.getSpans().stream()
                .mapToInt(s -> {
                    Integer v = input ? s.getInputTokens() : s.getOutputTokens();
                    return v == null ? 0 : v;
                })
                .sum();
    }

    public Trace requireTrace(UUID id) {
        return traceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trace not found"));
    }
}
