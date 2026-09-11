package com.llmobs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApiKeyRequest {
    @NotBlank
    private String name;
}
