package com.wokoba.czh.infrastructure.gateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelListResponseDTO {
    private String object;
    private List<ModelInfo> data;

    @Data
    public static class ModelInfo {
        private String id;
        private String object;
        private long created;
        private String owned_by;
    }
}