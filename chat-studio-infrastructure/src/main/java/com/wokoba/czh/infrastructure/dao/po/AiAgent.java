package com.wokoba.czh.infrastructure.dao.po;

import java.time.LocalDateTime;

/**
 * AI智能体配置表
 */
public class AiAgent {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}