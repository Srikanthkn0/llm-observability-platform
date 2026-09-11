package com.llmobs.service;

import com.llmobs.exception.ApiException;
import com.llmobs.model.Project;
import com.llmobs.model.ProjectMembership;
import com.llmobs.repository.ProjectMembershipRepository;
import com.llmobs.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository membershipRepository;

    public List<Project> listForUser(UUID userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(m -> projectRepository.findById(m.getProjectId()).orElse(null))
                .filter(p -> p != null)
                .toList();
    }

    public Project get(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public void requireMembership(UUID projectId, UUID userId) {
        membershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Not a member of this project"));
    }

    public ProjectMembership membership(UUID projectId, UUID userId) {
        return membershipRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Not a member of this project"));
    }
}
