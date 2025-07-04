package com.wokoba.czh.domain.agent.model.entity;

import com.wokoba.czh.domain.agent.model.valobj.AiClientOptionsVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientMateriel {

    private Long clientId;

    private Long systemPromptId;

    private String systemPromptContent;

    private Long modelId;

    private List<Long> mcpIdList;

    private List<Long> advisorIdList;

    private AiClientOptionsVO options;


    public String getModelBeanName() {
        return "AiClientModel_" + modelId;
    }

    public List<String> getMcpBeanNameList() {
        List<String> beanNameList = new ArrayList<>();
        for (Long mcpBeanId : mcpIdList) {
            beanNameList.add("AiClientToolMcp_" + mcpBeanId);
        }
        return beanNameList;
    }

    public List<String> getAdvisorBeanNameList() {
        List<String> beanNameList = new ArrayList<>();
        for (Long mcpBeanId : advisorIdList) {
            beanNameList.add("AiClientAdvisor_" + mcpBeanId);
        }
        return beanNameList;
    }
}
