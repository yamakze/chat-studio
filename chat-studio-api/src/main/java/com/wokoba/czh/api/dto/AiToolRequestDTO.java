package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.group.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class AiToolRequestDTO {
    /**
     * 主键ID
     */
    @Null(groups = Groups.Create.class,message = "创建时id必须为空")
    @NotNull(groups = Groups.Update.class,message = "更新时id不能为空")
    private Long id;

    /**
     * MCP名称
     */
    @NotBlank
    private String mcpName;

    /**
     * 传输类型(sse/stdio)
     */
    @NotBlank
    private String transportType;

    /**
     * 传输配置
     */
    @NotBlank
    private String transportConfig;

    /**
     * 请求超时时间(分钟)
     */
    private Integer requestTimeout;

    /**
     * 基础工具标志
     */
    private Boolean basic;
}
