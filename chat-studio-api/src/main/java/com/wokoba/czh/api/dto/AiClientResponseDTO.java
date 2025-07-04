package com.wokoba.czh.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiClientResponseDTO {
    private Long id;
    private Long modelId;
    private Long systemPromptId;
    private List<Long> mcpIds;
    private List<Long> advisorIds;
    private String clientName;
    private String description;
    private String optionsJsonStr;
}
