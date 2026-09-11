package com.llmobs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAlertRuleRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String metric;
    @NotBlank
    private String operator;
    @NotNull
    private BigDecimal threshold;
    private Integer windowMinutes = 15;
    private String slackWebhook;
}
