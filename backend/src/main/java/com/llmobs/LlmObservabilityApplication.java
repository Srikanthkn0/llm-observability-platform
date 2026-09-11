package com.llmobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LlmObservabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmObservabilityApplication.class, args);
    }
}
