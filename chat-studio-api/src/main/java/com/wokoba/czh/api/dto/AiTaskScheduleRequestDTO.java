package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.group.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class AiTaskScheduleRequestDTO {
    /**
     * 主键ID
     */
    @Null(groups = Groups.Create.class, message = "创建时id必须为空")
    @NotNull(groups = Groups.Update.class, message = "更新时id不能为空")
    private Long id;

    /**
     * 智能体ID
     */
    @NotNull
    private Long agentId;

    /**
     * 任务名称
     */
    @NotBlank
    private String taskName;

    /**
     * 任务描述
     */
    @NotBlank
    private String description;

    /**
     * 时间表达式(如: 0/3 * * * * *)
     */
    @NotBlank
    private String cronExpression;

    /**
     * 任务入参配置(JSON格式)
     */
    @NotBlank
    private String taskParam;

    /**
     * 状态(0:无效,1:有效)
     */
    @Null(groups = Groups.Create.class, message = "创建时status必须为null")
    private Integer status;
}
