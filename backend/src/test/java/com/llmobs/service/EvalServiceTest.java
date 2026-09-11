package com.llmobs.service;

import com.llmobs.model.Span;
import com.llmobs.model.Trace;
import com.llmobs.model.TraceStatus;
import com.llmobs.repository.EvaluationRepository;
import com.llmobs.repository.SpanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvalServiceTest {
    @Mock EvaluationRepository evaluationRepository;
    @Mock SpanRepository spanRepository;
    EvalService service;

    @BeforeEach
    void setUp() {
        service = new EvalService(evaluationRepository, spanRepository);
        ReflectionTestUtils.setField(service, "openAiApiKey", "");
        when(evaluationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void producesFiveEvalTypes() {
        UUID traceId = UUID.randomUUID();
        Trace trace = Trace.builder()
                .id(traceId)
                .projectId(UUID.randomUUID())
                .status(TraceStatus.PROCESSED)
                .latencyMs(400)
                .estimatedCostUsd(new BigDecimal("0.002"))
                .build();
        when(spanRepository.findByTraceIdOrderByStartedAtAsc(traceId)).thenReturn(List.of(
                Span.builder().inputText("Explain photosynthesis briefly")
                        .outputText("Photosynthesis converts light energy into chemical energy in plants based on chlorophyll.")
                        .build()
        ));

        var evals = service.evaluateTrace(trace);
        assertThat(evals).hasSize(5);
        assertThat(evals).extracting("evalType")
                .containsExactlyInAnyOrder("quality", "safety", "grounding", "performance", "cost");
        assertThat(evals).allMatch(e -> "deterministic".equals(e.getJudge()));
    }
}
