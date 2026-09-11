package com.llmobs.repository;

import com.llmobs.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByProjectIdAndRevokedAtIsNull(UUID projectId);
    List<ApiKey> findByKeyPrefixAndRevokedAtIsNull(String keyPrefix);
    Optional<ApiKey> findByIdAndProjectId(UUID id, UUID projectId);
}
