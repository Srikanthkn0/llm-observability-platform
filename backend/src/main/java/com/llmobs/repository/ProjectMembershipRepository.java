package com.llmobs.repository;

import com.llmobs.model.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {
    List<ProjectMembership> findByUserId(UUID userId);
    Optional<ProjectMembership> findByProjectIdAndUserId(UUID projectId, UUID userId);
}
