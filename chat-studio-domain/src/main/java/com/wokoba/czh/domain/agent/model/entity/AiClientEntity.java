package com.wokoba.czh.domain.agent.model.entity;

import com.wokoba.czh.domain.agent.model.valobj.AiClientOptionsVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientEntity {

    private Long clientId;

    private Long systemPromptId;

    private String systemPromptContent;

    private Long modelId;

    private String modelVersion;

    private List<Long> mcpIdList;

    private List<Long> advisorIdList;

    private String options;

    private String clientName;

    private String description;

    private Integer status;
}
