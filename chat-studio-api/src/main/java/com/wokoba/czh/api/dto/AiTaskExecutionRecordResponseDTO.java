package com.wokoba.czh.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiTaskExecutionRecordResponseDTO {
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
