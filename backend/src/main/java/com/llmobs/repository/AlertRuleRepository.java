package com.llmobs.repository;

import com.llmobs.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {
    List<AlertRule> findByProjectId(UUID projectId);
    List<AlertRule> findByEnabledTrue();
}
