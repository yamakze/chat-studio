package com.wokoba.czh.api.dto;

import lombok.Data;

@Data
public class AiModelResponseDTO {
    private Long id;
    private String modelVersion;
    private String modelName;
}
