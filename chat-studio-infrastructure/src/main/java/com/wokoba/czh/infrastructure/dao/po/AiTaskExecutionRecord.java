package com.wokoba.czh.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务执行记录实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiTaskExecutionRecord {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 请求内容
     */
    private String request;

    /**
     * 响应内容
     */
    private String response;

    /**
     * 总令牌数
     */
    private Integer totalTokens;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

}