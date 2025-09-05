package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.annotation.ExistModelVersion;
import com.wokoba.czh.api.common.ApiConstants;
import com.wokoba.czh.api.group.ValidatorGroups;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiClientConfigRequestDTO {
    @NotNull
    private Long clientId;
    @NotNull
    private Long systemPromptId;
    @Pattern(regexp = "^\\d+_[a-zA-Z0-9._-]+$", groups = ValidatorGroups.FirstValidationGroup.class)
    @ExistModelVersion(groups = ValidatorGroups.SecondValidationGroup.class)
    private String modelVersionId;
    @NotNull
    private Map<String, Object> options;
    private List<Long> advisorIds;
    private List<Long> mcpIds;

    public String getModelVersion() {
        return modelVersionId.split(ApiConstants.CONNECT)[1];
    }

    public Long getModelId() {
        return Long.valueOf(modelVersionId.split(ApiConstants.CONNECT)[0]);
    }
}
