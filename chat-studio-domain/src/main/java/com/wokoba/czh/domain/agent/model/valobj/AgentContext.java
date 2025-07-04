package com.wokoba.czh.domain.agent.model.valobj;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentContext {
    private ChatClient chatClient;
    private String userMessage;
    private String conversationId;
    private StringBuilder result;
    private AgentStatus status;
    private List<String> planSteps; // 规划的步骤
    private List<ExecutionRecord> executedSteps; // 已执行的步骤及其结果
    private String currentStep; // 当前正在执行的步骤
    private String currentStepResult; // 当前步骤的执行结果
    private List<ToolCallback> registeredTools;

    public static AgentContext create(Long clientId, String input, ChatClient client) {
        return new AgentContext(client, input, "chat_agent_" + clientId);
    }

    public AgentContext(ChatClient chatClient, String userMessage, String conversationId) {
        this.chatClient = chatClient;
        this.userMessage = userMessage;
        this.conversationId = conversationId;
        this.registeredTools = new ArrayList<>();
        this.result = new StringBuilder();
        this.status = AgentStatus.PLANNING;
        this.planSteps = new ArrayList<>();
        this.executedSteps = new ArrayList<>();
    }


    public void addExecutedStep(String step, String result) {
        this.executedSteps.add(new ExecutionRecord(step, result));
    }


    public void appendResult(String result) {
        this.result.append(result);
    }

    public record ExecutionRecord(String step, String result) {

    }
}
