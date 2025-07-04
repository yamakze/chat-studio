package com.wokoba.czh.infrastructure.dao.po;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户端-顾问关联表
 */
@Data
public class AiClientAdvisorConfig  {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 顾问ID
     */
    private Long advisorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}