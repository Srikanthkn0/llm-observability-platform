package com.llmobs.dto;

import com.llmobs.model.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;
    private String displayName;
    private Role role;
}
