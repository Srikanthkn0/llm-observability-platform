package com.llmobs.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Value("${llmobs.kafka.topics.traces-ingested}")
    private String tracesIngested;
    @Value("${llmobs.kafka.topics.traces-processed}")
    private String tracesProcessed;
    @Value("${llmobs.kafka.topics.traces-dlq}")
    private String tracesDlq;
    @Value("${llmobs.kafka.topics.evals-requested}")
    private String evalsRequested;

    @Bean
    public NewTopic tracesIngestedTopic() {
        return TopicBuilder.name(tracesIngested).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic tracesProcessedTopic() {
        return TopicBuilder.name(tracesProcessed).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic tracesDlqTopic() {
        return TopicBuilder.name(tracesDlq).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic evalsRequestedTopic() {
        return TopicBuilder.name(evalsRequested).partitions(3).replicas(1).build();
    }
}
