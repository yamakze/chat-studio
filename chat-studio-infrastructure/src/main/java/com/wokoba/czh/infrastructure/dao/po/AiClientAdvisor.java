package com.wokoba.czh.infrastructure.dao.po;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 顾问配置表
 */
@Data
public class AiClientAdvisor  {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 顾问名称
     */
    @NotBlank
    private String advisorName;

    /**
     * 顾问类型(PromptChatMemory/RagAnswer/SimpleLoggerAdvisor等)
     */
    @NotBlank
    private String advisorType;


    /**
     * 扩展参数配置，json 记录
     */
    @NotBlank
    private String extParam;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否是基础顾问
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