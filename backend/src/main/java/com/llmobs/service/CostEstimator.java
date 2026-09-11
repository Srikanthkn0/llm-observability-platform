package com.llmobs.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class CostEstimator {
    private static final Map<String, double[]> PRICES = Map.of(
            "gpt-4o", new double[]{0.005, 0.015},
            "gpt-4o-mini", new double[]{0.00015, 0.0006},
            "gpt-3.5-turbo", new double[]{0.0005, 0.0015},
            "claude-3-5-sonnet", new double[]{0.003, 0.015},
            "claude-3-haiku", new double[]{0.00025, 0.00125},
            "default", new double[]{0.001, 0.002}
    );

    public BigDecimal estimate(String model, int inputTokens, int outputTokens) {
        String key = model == null ? "default" : model.toLowerCase();
        double[] rates = PRICES.entrySet().stream()
                .filter(e -> key.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(PRICES.get("default"));
        double cost = (inputTokens / 1000.0) * rates[0] + (outputTokens / 1000.0) * rates[1];
        return BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP);
    }
}
