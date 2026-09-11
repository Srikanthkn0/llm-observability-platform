package com.llmobs.service;

import com.llmobs.dto.DashboardMetricsDto;
import com.llmobs.repository.EvaluationRepository;
import com.llmobs.repository.GuardrailDecisionRepository;
import com.llmobs.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final TraceRepository traceRepository;
    private final EvaluationRepository evaluationRepository;
    private final GuardrailDecisionRepository guardrailDecisionRepository;

    public DashboardMetricsDto dashboard(UUID projectId) {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        BigDecimal cost = traceRepository.sumCostSince(projectId, since);
        Double avgLatency = traceRepository.avgLatencySince(projectId, since);
        Double quality = evaluationRepository.avgScoreByTypeSince(projectId, "quality", since);
        Double safety = evaluationRepository.avgScoreByTypeSince(projectId, "safety", since);
        return DashboardMetricsDto.builder()
                .tracesLast24h(traceRepository.countByProjectIdAndIngestedAtAfter(projectId, since))
                .blockedLast24h(traceRepository.countBlockedSince(projectId, since))
                .failedLast24h(traceRepository.countFailedSince(projectId, since))
                .evalFailuresLast24h(evaluationRepository.countByProjectIdAndPassedFalseAndCreatedAtAfter(projectId, since))
                .costLast24h(cost == null ? BigDecimal.ZERO : cost)
                .avgLatencyMs(avgLatency == null ? 0 : avgLatency)
                .avgQualityScore(quality)
                .avgSafetyScore(safety)
                .guardrailBlocksTotal(guardrailDecisionRepository.countByProjectIdAndActionAndMatchedTrue(projectId, "BLOCK"))
                .build();
    }
}
