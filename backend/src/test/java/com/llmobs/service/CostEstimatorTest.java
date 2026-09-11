package com.llmobs.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class CostEstimatorTest {
    private final CostEstimator estimator = new CostEstimator();

    @Test
    void estimatesGpt4oMini() {
        BigDecimal cost = estimator.estimate("gpt-4o-mini", 1000, 1000);
        assertThat(cost).isEqualByComparingTo("0.000750");
    }

    @Test
    void usesDefaultForUnknownModel() {
        BigDecimal cost = estimator.estimate("mystery-model", 1000, 0);
        assertThat(cost).isEqualByComparingTo("0.001000");
    }
}
