package com.wokoba.czh.api.dto;

import com.wokoba.czh.api.group.Groups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class AiModelRequestDTO {
    /**
     * 主键ID
     */
    @Null(groups = Groups.Create.class,message = "创建时id必须为空")
    @NotNull(groups = Groups.Update.class,message = "更新时id不能为空")
    private Long id;

    /**
     * 模型名称
     */
    @NotBlank
    private String modelName;

    /**
     * 基础URL
     */
    @NotBlank
    private String baseUrl;

    /**
     * API密钥
     */
    @NotBlank
    private String apiKey;

    /**
     * 完成路径
     */
    @NotBlank
    private String completionsPath;

    /**
     * 模型类型(openai/azure等)
     */
    @NotBlank
    private String modelType;

    /**
     * 模型版本
     */
    @NotBlank(groups = Groups.Update.class,message = "更新时模型版本不能为空")
    private String modelVersion;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;
}
