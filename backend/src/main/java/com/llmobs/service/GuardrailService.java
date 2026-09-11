package com.llmobs.service;

import com.llmobs.model.GuardrailDecision;
import com.llmobs.repository.GuardrailDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GuardrailService {
    private final GuardrailDecisionRepository guardrailDecisionRepository;

    private static final Pattern PII_EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PII_SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern PII_PHONE = Pattern.compile("\\b(?:\\+?1[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b");
    private static final List<String> INJECTION_PHRASES = List.of(
            "ignore previous instructions", "ignore all instructions", "you are now",
            "system prompt", "jailbreak", "dan mode", "bypass safety", "disregard your rules"
    );
    private static final List<String> TOXICITY_TERMS = List.of(
            "kill yourself", "i hate you", "you idiot", "stupid bot", "go die"
    );

    public record GuardrailResult(boolean blocked, List<GuardrailDecision> decisions) {}

    public GuardrailResult evaluate(UUID projectId, UUID traceId, String inputText, String outputText) {
        List<GuardrailDecision> decisions = new ArrayList<>();
        String combined = ((inputText == null ? "" : inputText) + " " + (outputText == null ? "" : outputText)).toLowerCase();

        decisions.add(checkInjection(projectId, traceId, combined, inputText));
        decisions.add(checkPii(projectId, traceId, inputText, outputText));
        decisions.add(checkToxicity(projectId, traceId, combined));

        boolean blocked = decisions.stream().anyMatch(d -> d.isMatched() && "BLOCK".equals(d.getAction()));
        guardrailDecisionRepository.saveAll(decisions);
        return new GuardrailResult(blocked, decisions);
    }

    private GuardrailDecision checkInjection(UUID projectId, UUID traceId, String combined, String input) {
        boolean matched = INJECTION_PHRASES.stream().anyMatch(combined::contains);
        return GuardrailDecision.builder()
                .projectId(projectId)
                .traceId(traceId)
                .ruleName("prompt_injection")
                .category("injection")
                .action(matched ? "BLOCK" : "ALLOW")
                .matched(matched)
                .severity(matched ? "HIGH" : "INFO")
                .detailsJson(Map.of(
                        "phrasesChecked", INJECTION_PHRASES.size(),
                        "sample", truncate(input, 120)
                ))
                .build();
    }

    private GuardrailDecision checkPii(UUID projectId, UUID traceId, String input, String output) {
        String text = (input == null ? "" : input) + " " + (output == null ? "" : output);
        boolean email = PII_EMAIL.matcher(text).find();
        boolean ssn = PII_SSN.matcher(text).find();
        boolean phone = PII_PHONE.matcher(text).find();
        boolean matched = email || ssn || phone;
        return GuardrailDecision.builder()
                .projectId(projectId)
                .traceId(traceId)
                .ruleName("pii_detection")
                .category("pii")
                .action(matched ? "FLAG" : "ALLOW")
                .matched(matched)
                .severity(matched ? "MEDIUM" : "INFO")
                .detailsJson(Map.of("email", email, "ssn", ssn, "phone", phone))
                .build();
    }

    private GuardrailDecision checkToxicity(UUID projectId, UUID traceId, String combined) {
        boolean matched = TOXICITY_TERMS.stream().anyMatch(combined::contains);
        return GuardrailDecision.builder()
                .projectId(projectId)
                .traceId(traceId)
                .ruleName("toxicity")
                .category("toxicity")
                .action(matched ? "FLAG" : "ALLOW")
                .matched(matched)
                .severity(matched ? "MEDIUM" : "INFO")
                .detailsJson(Map.of("termsChecked", TOXICITY_TERMS.size()))
                .build();
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}
