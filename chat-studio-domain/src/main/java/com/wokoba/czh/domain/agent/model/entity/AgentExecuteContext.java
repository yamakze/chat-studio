package com.wokoba.czh.domain.agent.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public  class AgentExecuteContext {

    // 任务执行步骤
    private int step = 1;

    // 最大任务步骤
    private int maxStep = 1;

    private StringBuilder executionHistory;

    private String currentTask;

    private ExecutionResult currentResult;

    boolean isCompleted = false;

    private Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap;

    private Map<String, Object> dataObjects = new HashMap<>();

    public <T> void setValue(String key, T value) {
        dataObjects.put(key, value);
    }

    public <T> T getValue(String key) {
        return (T) dataObjects.get(key);
    }

    public record ExecutionResult(@JsonProperty("输出结果") String outPut, @JsonProperty("任务状态") String taskStatus){

    }
}

