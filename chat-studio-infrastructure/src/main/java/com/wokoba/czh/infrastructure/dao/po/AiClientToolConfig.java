package com.wokoba.czh.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户端-工具关联表
 */
@Data
public class AiClientToolConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 工具类型(mcp/function call)
     */
    private String toolType;

    /**
     * 工具ID(MCP ID/function call ID)
     */
    private Long toolId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}