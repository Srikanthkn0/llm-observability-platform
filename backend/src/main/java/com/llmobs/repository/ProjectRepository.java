package com.llmobs.repository;

import com.llmobs.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findBySlug(String slug);
    List<Project> findByOwnerId(UUID ownerId);
}
