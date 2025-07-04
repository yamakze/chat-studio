package com.wokoba.czh.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class AiModelUpdateRequestDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模型名称
     */
    @NotBlank
    private String modelName;

    /**
     * 基础URL
     */
    @NotBlank
    private String baseUrl;

    /**
     * API密钥
     */
    @NotBlank
    private String apiKey;

    /**
     * 完成路径
     */
    @NotBlank()
    private String completionsPath;

    /**
     * 模型类型(openai/azure等)
     */
    @NotBlank
    private String modelType;

    /**
     * 模型版本
     */
    @NotBlank
    private String modelVersion;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;
}
