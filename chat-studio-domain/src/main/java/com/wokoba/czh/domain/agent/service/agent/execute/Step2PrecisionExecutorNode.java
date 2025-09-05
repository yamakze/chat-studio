package com.wokoba.czh.domain.agent.service.agent.execute;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.wokoba.czh.domain.agent.model.entity.AgentExecuteContext;
import com.wokoba.czh.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.wokoba.czh.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity;
import com.wokoba.czh.domain.agent.model.valobj.AiClientTypeEnumVO;
import com.wokoba.czh.domain.agent.service.agent.AbstractAgentExecuteSupport;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Step2PrecisionExecutorNode extends AbstractAgentExecuteSupport {
    @Resource
    private Step3QualitySupervisorNode step3QualitySupervisorNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        log.info("\n⚡ 阶段2: 精准任务执行");

        // 从动态上下文中获取分析结果
        String analysisResult = dynamicContext.getValue("analysisResult");
        if (analysisResult == null || analysisResult.trim().isEmpty()) {
            log.warn("⚠️ 分析结果为空，使用默认执行策略");
            analysisResult = "执行当前任务步骤";
        }

        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getAiAgentClientFlowConfigVOMap().get(AiClientTypeEnumVO.PRECISION_EXECUTOR_CLIENT.getCode());

        String executionPrompt = String.format(aiAgentClientFlowConfigVO.getStepPrompt(), requestParameter.getMessage(), analysisResult);

        // 获取对话客户端
        ChatClient chatClient = getChatClientByClientId(aiAgentClientFlowConfigVO.getClientId());

        ExecutionResult executionResult = chatClient
                .prompt(executionPrompt)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, requestParameter.getSessionId()))
                .call()
                .entity(ExecutionResult.class);

        assert executionResult != null;
        String parseToString = executionResult.parseToString();
        // 将执行结果保存到动态上下文中，供下一步使用
        dynamicContext.setValue("executionResult", parseToString);

        sendExecutionSubResult(dynamicContext, parseToString, requestParameter.getSessionId());
        // 更新执行历史
        String stepSummary = String.format("""
                === 第 %d 步执行记录 ===
                【分析阶段】%s
                【执行阶段】%s
                """, dynamicContext.getStep(), parseToString, executionResult);

        dynamicContext.getExecutionHistory().append(stepSummary);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ExecuteCommandEntity, AgentExecuteContext, String> get(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        return step3QualitySupervisorNode;
    }

    /**
     * 发送执行阶段细分结果到流式输出
     */
    private void sendExecutionSubResult(AgentExecuteContext dynamicContext,
                                        String content, String sessionId) {
        if (!content.isBlank()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createExecutionSubResult(
                    dynamicContext.getStep(), content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }

    private record ExecutionResult(@JsonProperty("明确的执行目标") String executionTarget,
                                   @JsonProperty("实际执行的步骤和调用的工具") String executionProcess,
                                   @JsonProperty("具体的执行成果和获得的信息/内容") String executionResult,
                                   @JsonProperty("对执行结果的质量评估") String executionQuality) {
        String parseToString() {
            String result = "执行目标: " + executionTarget + "\n" +
                            "执行过程: " + executionProcess + "\n" +
                            "执行结果: " + executionResult + "\n" +
                            "完成度评估: " + executionQuality;

            log.info("解析结果 -> \n{}", result);
            return result;
        }
    }

}
