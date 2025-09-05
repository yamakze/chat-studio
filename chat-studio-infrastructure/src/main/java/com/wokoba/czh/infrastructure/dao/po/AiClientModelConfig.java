package com.wokoba.czh.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@TableName("ai_client_model_config")
public class AiClientModelConfig {

    private Long id;

    private Long clientId;

    private Long modelId;

    private String modelVersion;

    private String options;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
