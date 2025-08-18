package com.wokoba.czh.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能体-客户端关联表
 */
@Data
public class AiAgentFlowConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体ID
     */
    private Long agentId;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 序列号(执行顺序)
     */
    private Integer sequence;

    /**
     * 步骤提示词
     */
    private String stepPrompt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}