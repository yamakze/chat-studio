package com.wokoba.czh.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiClientConfigRequestDTO {
    @NotNull
    private Long clientId;
    private Long systemPromptId;
    private Long modelId;
    @NotNull
    private Map<String, Object> options;
    private List<Long> advisorIds;
    private List<Long> mcpIds;
}
