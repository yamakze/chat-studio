package com.wokoba.czh.domain.agent.service.agent.Impl;

import com.wokoba.czh.domain.agent.model.valobj.AgentContext;
import com.wokoba.czh.domain.agent.model.valobj.AgentStatus;
import com.wokoba.czh.domain.agent.model.valobj.PromptTemplates;
import com.wokoba.czh.domain.agent.service.agent.AbstractAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeneralAgentService extends AbstractAgentService {

    @Override
    protected void execute(AgentContext context) {
        if (context.getPlanSteps().isEmpty()) {
            context.appendResult("没有规划步骤，任务直接完成。");
            context.setStatus(AgentStatus.COMPLETED);
            return;
        }

        for (int i = 0; i < MAX_STEPS && context.getStatus() == AgentStatus.EXECUTING; i++) {
            if (context.getPlanSteps().isEmpty()) { // 所有步骤执行完毕
                context.setStatus(AgentStatus.COMPLETED);
                break;
            }
            if (executeStep(context)) break;
        }

        if (context.getStatus() == AgentStatus.EXECUTING) { // 如果循环结束但状态仍为EXECUTING，说明达到最大步数或无更多步骤
            if (!context.getPlanSteps().isEmpty()) {
                context.appendResult("达到最大执行步数！");
                context.setStatus(AgentStatus.STOPPED);
                log.warn("达到最大执行步数，任务未完全完成 剩余:{} 步", context.getPlanSteps().size());
            } else {
                context.setStatus(AgentStatus.COMPLETED);
                context.appendResult("所有规划步骤已执行完成。");
            }
        }
    }

    private boolean executeStep(AgentContext context) {
        String currentStep = context.getPlanSteps().remove(0); // 取出并移除当前步骤
        ChatClient chatClient = context.getChatClient();
        context.setCurrentStep(currentStep);

        try {
            // 1. 获取完整的 ChatResponse
            ChatResponse response = chatClient.prompt()
                    .system(PromptTemplates.STEP.getTemplate())
                    .user(user -> user.text("Now please agentChat:{step}.")
                            .params(Map.of("step", currentStep)))
                    .toolCallbacks(registeredTools)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, context.getConversationId()))
                    .call()
                    .chatResponse();

            // 2. 从响应中获取结果内容
            String stepResult = response.getResult().getOutput().getText();

            context.setCurrentStepResult(stepResult);
            context.addExecutedStep(currentStep, stepResult);

        } catch (Exception e) {
            context.setStatus(AgentStatus.FAILED);
            context.appendResult("步骤执行失败！");
            log.error("执行 {} 步骤时失败", currentStep, e);
            return true;
        }
        return false;
    }

    @Override
    protected void plan(AgentContext context) {
        context.setStatus(AgentStatus.PLANNING);
        ChatClient chatClient = context.getChatClient();
        try {
            List<String> stepList = chatClient.prompt()
                    .system(PromptTemplates.PLAN.getTemplate())
                    .user(context.getUserMessage())
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, context.getConversationId()))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });
            context.setPlanSteps(stepList);
            context.setStatus(AgentStatus.EXECUTING); // 规划成功，进入执行状态
        } catch (Exception e) {
            context.setStatus(AgentStatus.FAILED);
            log.error("规划失败 context:{}", context, e);
        }
    }
}
