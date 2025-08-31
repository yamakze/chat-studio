package com.wokoba.czh.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiModelResponseDTO {
    private Long id;
    private List<String> modelVersionList;
    private String modelName;
}
