package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.group.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class AiPromptRequestDTO {
    /**
     * 主键ID
     */
    @Null(groups = Groups.Create.class,message = "创建时id必须为空")
    @NotNull(groups = Groups.Update.class,message = "更新时id不能为空")
    private Long id;

    /**
     * 提示词名称
     */
    @NotBlank
    private String promptName;

    /**
     * 提示词内容
     */
    @NotBlank
    private String promptContent;

    /**
     * 描述
     */
    @NotBlank
    private String description;

    /**
     * 状态(0:禁用,1:启用)
     */
    @Null(groups = Groups.Create.class,message = "创建时status必须为null")
    private Integer status;
}
