package com.llmobs.repository;

import com.llmobs.model.AlertEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AlertEventRepository extends JpaRepository<AlertEvent, UUID> {
    Page<AlertEvent> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
