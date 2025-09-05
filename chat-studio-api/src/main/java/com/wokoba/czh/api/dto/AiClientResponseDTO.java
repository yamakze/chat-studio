package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.common.ApiConstants;
import lombok.Data;

import java.util.List;

@Data
public class AiClientResponseDTO {
    private Long id;
    private String modelVersionId;
    private Long systemPromptId;
    private List<Long> mcpIds;
    private List<Long> advisorIds;
    private String clientName;
    private String description;
    private String optionsJsonStr;

    public void setModelVersionId(Long modelId, String modelVersion) {
        this.modelVersionId = modelId + ApiConstants.CONNECT + modelVersion;
    }
}
