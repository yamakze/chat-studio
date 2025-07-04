package com.wokoba.czh.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiClient {
    private Long id;
    private Long modelId;
    private Long systemPromptId;
    private String clientName;
    private String options;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
