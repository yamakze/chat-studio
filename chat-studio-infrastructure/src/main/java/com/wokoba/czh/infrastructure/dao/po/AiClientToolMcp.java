package com.wokoba.czh.infrastructure.dao.po;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MCP客户端配置表
 */
@Data
public class AiClientToolMcp {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * MCP名称
     */
    private String mcpName;

    /**
     * 传输类型(sse/stdio)
     */
    private String transportType;

    /**
     * 传输配置
     */
    private String transportConfig;

    /**
     * 请求超时时间(分钟)
     */
    private Integer requestTimeout;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 基础工具标志
     */
    private Boolean basic;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}