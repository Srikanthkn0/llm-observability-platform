package com.llmobs.service;

import com.llmobs.dto.*;
import com.llmobs.exception.ApiException;
import com.llmobs.model.*;
import com.llmobs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraceQueryService {
    private final TraceRepository traceRepository;
    private final SpanRepository spanRepository;
    private final EvaluationRepository evaluationRepository;
    private final GuardrailDecisionRepository guardrailDecisionRepository;

    public PageResponse<TraceSummaryDto> list(UUID projectId, TraceStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Trace> result = status == null
                ? traceRepository.findByProjectIdOrderByIngestedAtDesc(projectId, pr)
                : traceRepository.findByProjectIdAndStatusOrderByIngestedAtDesc(projectId, status, pr);
        return PageResponse.<TraceSummaryDto>builder()
                .content(result.getContent().stream().map(this::toSummary).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public TraceDetailDto getDetail(UUID projectId, UUID traceId) {
        Trace trace = traceRepository.findById(traceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trace not found"));
        if (!trace.getProjectId().equals(projectId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Trace not in project");
        }
        List<SpanDto> spans = spanRepository.findByTraceIdOrderByStartedAtAsc(traceId).stream()
                .map(this::toSpan).toList();
        List<EvaluationDto> evals = evaluationRepository.findByTraceId(traceId).stream()
                .map(this::toEval).toList();
        List<GuardrailDecisionDto> guards = guardrailDecisionRepository.findByTraceId(traceId).stream()
                .map(this::toGuard).toList();
        return TraceDetailDto.builder()
                .id(trace.getId())
                .projectId(trace.getProjectId())
                .name(trace.getName())
                .status(trace.getStatus())
                .model(trace.getModel())
                .provider(trace.getProvider())
                .inputTokens(trace.getInputTokens())
                .outputTokens(trace.getOutputTokens())
                .totalTokens(trace.getTotalTokens())
                .estimatedCostUsd(trace.getEstimatedCostUsd())
                .latencyMs(trace.getLatencyMs())
                .userIdExt(trace.getUserIdExt())
                .sessionId(trace.getSessionId())
                .metadata(trace.getMetadataJson())
                .startedAt(trace.getStartedAt())
                .endedAt(trace.getEndedAt())
                .ingestedAt(trace.getIngestedAt())
                .processedAt(trace.getProcessedAt())
                .errorMessage(trace.getErrorMessage())
                .spans(spans)
                .evaluations(evals)
                .guardrails(guards)
                .build();
    }

    private TraceSummaryDto toSummary(Trace t) {
        return TraceSummaryDto.builder()
                .id(t.getId())
                .name(t.getName())
                .status(t.getStatus())
                .model(t.getModel())
                .provider(t.getProvider())
                .totalTokens(t.getTotalTokens())
                .estimatedCostUsd(t.getEstimatedCostUsd())
                .latencyMs(t.getLatencyMs())
                .ingestedAt(t.getIngestedAt())
                .build();
    }

    private SpanDto toSpan(Span s) {
        return SpanDto.builder()
                .id(s.getId())
                .parentSpanId(s.getParentSpanId())
                .name(s.getName())
                .kind(s.getKind())
                .status(s.getStatus())
                .model(s.getModel())
                .inputText(s.getInputText())
                .outputText(s.getOutputText())
                .inputTokens(s.getInputTokens())
                .outputTokens(s.getOutputTokens())
                .latencyMs(s.getLatencyMs())
                .attributes(s.getAttributesJson())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .build();
    }

    private EvaluationDto toEval(Evaluation e) {
        return EvaluationDto.builder()
                .id(e.getId())
                .traceId(e.getTraceId())
                .evalType(e.getEvalType())
                .score(e.getScore())
                .passed(e.getPassed())
                .judge(e.getJudge())
                .rationale(e.getRationale())
                .details(e.getDetailsJson())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private GuardrailDecisionDto toGuard(GuardrailDecision g) {
        return GuardrailDecisionDto.builder()
                .id(g.getId())
                .traceId(g.getTraceId())
                .ruleName(g.getRuleName())
                .category(g.getCategory())
                .action(g.getAction())
                .matched(g.isMatched())
                .severity(g.getSeverity())
                .details(g.getDetailsJson())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
