package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiClientConfigRequestDTO {
    @NotNull
    private Long clientId;
    @NotNull
    private Long systemPromptId;
    @NotNull
    private Long modelId;
    @ExistModelVersion
    private String modelVersion;
    @NotNull
    private Map<String, Object> options;
    private List<Long> advisorIds;
    private List<Long> mcpIds;
}
