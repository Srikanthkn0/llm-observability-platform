package com.llmobs.service;

import com.llmobs.model.Evaluation;
import com.llmobs.model.Span;
import com.llmobs.model.Trace;
import com.llmobs.repository.EvaluationRepository;
import com.llmobs.repository.SpanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvalService {
    private final EvaluationRepository evaluationRepository;
    private final SpanRepository spanRepository;

    @Value("${llmobs.openai.api-key:}")
    private String openAiApiKey;

    public List<Evaluation> evaluateTrace(Trace trace) {
        List<Span> spans = spanRepository.findByTraceIdOrderByStartedAtAsc(trace.getId());
        String input = spans.stream().map(Span::getInputText).filter(Objects::nonNull).findFirst().orElse("");
        String output = spans.stream().map(Span::getOutputText).filter(Objects::nonNull)
                .reduce((a, b) -> b).orElse("");

        String judge = (openAiApiKey != null && !openAiApiKey.isBlank()) ? "openai" : "deterministic";
        if ("openai".equals(judge)) {
            log.info("OPENAI_API_KEY present — using deterministic judge as primary demo path (OpenAI optional)");
            judge = "deterministic";
        }

        List<Evaluation> evals = new ArrayList<>();
        evals.add(qualityEval(trace, input, output, judge));
        evals.add(safetyEval(trace, input, output, judge));
        evals.add(groundingEval(trace, input, output, judge));
        evals.add(performanceEval(trace, judge));
        evals.add(costEval(trace, judge));
        return evaluationRepository.saveAll(evals);
    }

    private Evaluation qualityEval(Trace trace, String input, String output, String judge) {
        double score = 0.5;
        List<String> notes = new ArrayList<>();
        if (output != null && output.length() > 20) { score += 0.2; notes.add("non-trivial output"); }
        if (output != null && !output.isBlank() && input != null && !input.isBlank()) {
            long overlap = Arrays.stream(input.toLowerCase().split("\\W+"))
                    .filter(w -> w.length() > 3)
                    .filter(w -> output.toLowerCase().contains(w))
                    .limit(5).count();
            score += Math.min(0.25, overlap * 0.05);
            notes.add("token overlap=" + overlap);
        }
        if (output != null && output.toLowerCase().contains("i don't know") && input.length() > 40) {
            score -= 0.15; notes.add("uncertainty phrase");
        }
        score = clamp(score);
        return build(trace, "quality", score, score >= 0.6, judge,
                "Heuristic quality: " + String.join("; ", notes), Map.of("notes", notes));
    }

    private Evaluation safetyEval(Trace trace, String input, String output, String judge) {
        String combined = ((input == null ? "" : input) + " " + (output == null ? "" : output)).toLowerCase();
        List<String> flags = new ArrayList<>();
        if (combined.contains("how to make a bomb") || combined.contains("build a weapon")) flags.add("harmful");
        if (combined.contains("steal password") || combined.contains("credit card dump")) flags.add("fraud");
        double score = flags.isEmpty() ? 0.95 : 0.2;
        return build(trace, "safety", score, flags.isEmpty(), judge,
                flags.isEmpty() ? "No safety issues detected" : "Flags: " + flags,
                Map.of("flags", flags));
    }

    private Evaluation groundingEval(Trace trace, String input, String output, String judge) {
        double score = 0.7;
        if (output != null && (output.contains("according to") || output.contains("based on"))) {
            score = 0.85;
        }
        if (output != null && output.toLowerCase().contains("as an ai") && output.length() < 40) {
            score = 0.4;
        }
        score = clamp(score);
        return build(trace, "grounding", score, score >= 0.55, judge,
                "Heuristic grounding score", Map.of());
    }

    private Evaluation performanceEval(Trace trace, String judge) {
        int latency = trace.getLatencyMs() == null ? 0 : trace.getLatencyMs();
        double score;
        if (latency <= 500) score = 1.0;
        else if (latency <= 1500) score = 0.85;
        else if (latency <= 3000) score = 0.65;
        else if (latency <= 8000) score = 0.4;
        else score = 0.2;
        return build(trace, "performance", score, score >= 0.5, judge,
                "Latency " + latency + "ms", Map.of("latencyMs", latency));
    }

    private Evaluation costEval(Trace trace, String judge) {
        BigDecimal cost = trace.getEstimatedCostUsd() == null ? BigDecimal.ZERO : trace.getEstimatedCostUsd();
        double score;
        if (cost.compareTo(new BigDecimal("0.01")) <= 0) score = 1.0;
        else if (cost.compareTo(new BigDecimal("0.05")) <= 0) score = 0.8;
        else if (cost.compareTo(new BigDecimal("0.25")) <= 0) score = 0.55;
        else score = 0.3;
        return build(trace, "cost", score, score >= 0.5, judge,
                "Estimated cost $" + cost, Map.of("costUsd", cost));
    }

    private Evaluation build(Trace trace, String type, double score, boolean passed, String judge,
                             String rationale, Map<String, Object> details) {
        return Evaluation.builder()
                .traceId(trace.getId())
                .projectId(trace.getProjectId())
                .evalType(type)
                .score(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP))
                .passed(passed)
                .judge(judge)
                .rationale(rationale)
                .detailsJson(details)
                .build();
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
