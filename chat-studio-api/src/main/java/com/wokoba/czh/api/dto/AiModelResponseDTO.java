package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.common.ApiConstants;
import lombok.Data;

@Data
public class AiModelResponseDTO {
    private String modelVersionId;
    private String modelName;
    private Long modelId;
    private String modelVersion;

    public void setModelVersionId(Long modelId, String modelVersion) {
        this.modelVersionId = modelId + ApiConstants.CONNECT + modelVersion;
        this.modelVersion = modelVersion;
        this.modelId = modelId;
    }
}
