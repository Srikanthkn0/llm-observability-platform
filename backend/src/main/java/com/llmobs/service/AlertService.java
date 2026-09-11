package com.llmobs.service;

import com.llmobs.dto.CreateAlertRuleRequest;
import com.llmobs.exception.ApiException;
import com.llmobs.model.AlertEvent;
import com.llmobs.model.AlertRule;
import com.llmobs.repository.AlertEventRepository;
import com.llmobs.repository.AlertRuleRepository;
import com.llmobs.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final TraceRepository traceRepository;

    public AlertRule create(UUID projectId, CreateAlertRuleRequest req) {
        return alertRuleRepository.save(AlertRule.builder()
                .projectId(projectId)
                .name(req.getName())
                .metric(req.getMetric())
                .operator(req.getOperator())
                .threshold(req.getThreshold())
                .windowMinutes(req.getWindowMinutes() == null ? 15 : req.getWindowMinutes())
                .enabled(true)
                .slackWebhook(req.getSlackWebhook())
                .build());
    }

    public List<AlertRule> listRules(UUID projectId) {
        return alertRuleRepository.findByProjectId(projectId);
    }

    public Page<AlertEvent> listEvents(UUID projectId, int page, int size) {
        return alertEventRepository.findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(page, size));
    }

    @Scheduled(fixedDelayString = "${llmobs.alerts.check-interval-ms:60000}")
    @Transactional
    public void checkRules() {
        for (AlertRule rule : alertRuleRepository.findByEnabledTrue()) {
            try {
                evaluate(rule);
            } catch (Exception e) {
                log.warn("Alert rule {} failed: {}", rule.getId(), e.getMessage());
            }
        }
    }

    private void evaluate(AlertRule rule) {
        Instant since = Instant.now().minus(rule.getWindowMinutes(), ChronoUnit.MINUTES);
        BigDecimal value = switch (rule.getMetric()) {
            case "cost_usd" -> {
                BigDecimal v = traceRepository.sumCostSince(rule.getProjectId(), since);
                yield v == null ? BigDecimal.ZERO : v;
            }
            case "trace_count" -> BigDecimal.valueOf(traceRepository.countByProjectIdAndIngestedAtAfter(rule.getProjectId(), since));
            case "avg_latency_ms" -> {
                Double avg = traceRepository.avgLatencySince(rule.getProjectId(), since);
                yield BigDecimal.valueOf(avg == null ? 0 : avg);
            }
            case "blocked_count" -> BigDecimal.valueOf(traceRepository.countBlockedSince(rule.getProjectId(), since));
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown metric: " + rule.getMetric());
        };

        if (thresholdBreached(value, rule.getOperator(), rule.getThreshold())) {
            String msg = String.format("Alert '%s': %s %s %s (actual=%s)",
                    rule.getName(), rule.getMetric(), rule.getOperator(), rule.getThreshold(), value);
            AlertEvent event = alertEventRepository.save(AlertEvent.builder()
                    .ruleId(rule.getId())
                    .projectId(rule.getProjectId())
                    .message(msg)
                    .metricValue(value)
                    .notified(false)
                    .build());
            notifySlack(rule, event);
        }
    }

    private boolean thresholdBreached(BigDecimal value, String op, BigDecimal threshold) {
        return switch (op) {
            case ">" -> value.compareTo(threshold) > 0;
            case ">=" -> value.compareTo(threshold) >= 0;
            case "<" -> value.compareTo(threshold) < 0;
            case "<=" -> value.compareTo(threshold) <= 0;
            case "==" -> value.compareTo(threshold) == 0;
            default -> false;
        };
    }

    private void notifySlack(AlertRule rule, AlertEvent event) {
        String url = rule.getSlackWebhook();
        if (url == null || url.isBlank()) {
            log.info("Slack webhook stub: no URL configured for rule {} — would post: {}", rule.getId(), event.getMessage());
            return;
        }
        // Stub: do not call external webhooks in demo; log the intended URL/target
        log.info("Slack webhook stub: would POST to {} payload={}", url, event.getMessage());
        event.setNotified(true);
        alertEventRepository.save(event);
    }
}
