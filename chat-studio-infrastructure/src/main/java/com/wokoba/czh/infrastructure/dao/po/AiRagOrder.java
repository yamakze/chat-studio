package com.wokoba.czh.infrastructure.dao.po;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库配置表
 */
@Data
public class AiRagOrder  {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    @NotBlank
    private String ragName;

    /**
     * 知识标签
     */
    @NotBlank
    private String knowledgeTag;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}