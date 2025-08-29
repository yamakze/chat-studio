package com.wokoba.czh.domain.agent.service.agent.execute;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.wokoba.czh.domain.agent.model.entity.AgentExecuteContext;
import com.wokoba.czh.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity;
import com.wokoba.czh.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
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
public class Step1AnalyzerNode extends AbstractAgentExecuteSupport {

    @Resource
    private Step2PrecisionExecutorNode step2PrecisionExecutorNode;
    @Resource
    private Step4LogExecutionSummaryNode step4LogExecutionSummaryNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        log.info("\n🎯 === 执行第 {} 步 ===", dynamicContext.getStep());

        // 获取配置信息
        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getAiAgentClientFlowConfigVOMap().get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());

        log.info("\n📊 阶段1: 任务状态分析");
        String analysisPrompt = String.format(aiAgentClientFlowConfigVO.getStepPrompt(),
                requestParameter.getMessage(),
                dynamicContext.getStep(),
                dynamicContext.getMaxStep(),
                !dynamicContext.getExecutionHistory().isEmpty() ? dynamicContext.getExecutionHistory().toString() : "[首次执行]",
                dynamicContext.getCurrentTask()
        );

        ChatClient chatClient = getChatClientByClientId(aiAgentClientFlowConfigVO.getClientId());

        AnalysisResult analysisResult = chatClient
                .prompt(analysisPrompt)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, requestParameter.getSessionId()))
                .call().entity(AnalysisResult.class);

        assert analysisResult != null;

        String parseToString = analysisResult.parseToString();
        sendAnalysisSubResult(dynamicContext, parseToString, requestParameter.getSessionId());
        // 将分析结果保存到动态上下文中，供下一步使用
        dynamicContext.setValue("AnalysisResult", parseToString);

        // 检查是否已完成
        if (analysisResult.taskStatus().equals("COMPLETED")) {
            dynamicContext.setCompleted(true);
            log.info("✅ 任务分析显示已完成！");
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ExecuteCommandEntity, AgentExecuteContext, String> get(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return step4LogExecutionSummaryNode;
        }

        // 否则继续执行下一步
        return step2PrecisionExecutorNode;
    }

    /**
     * 发送分析阶段细分结果到流式输出
     */
    private void sendAnalysisSubResult(AgentExecuteContext dynamicContext,
                                       String content, String sessionId) {
        if (!content.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    dynamicContext.getStep(), content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }

    private record AnalysisResult(@JsonProperty("当前任务完成情况的详细分析") String taskAnalysis,
                                  @JsonProperty("对已完成工作的质量和效果评估") String executionHistoryEvaluation,
                                  @JsonProperty("具体的执行计划，包括需要调用的工具和生成的内容") String nextStepStrategy,
                                  @JsonProperty("CONTINUE/COMPLETED") String taskStatus) {
        String parseToString() {

            String result = "任务状态分析: " + taskAnalysis + "\n" +
                            "执行历史评估: " + executionHistoryEvaluation + "\n" +
                            "下一步策略: " + nextStepStrategy + "\n" +
                            "完成度评估: " +
                            ("COMPLETED".equalsIgnoreCase(taskStatus) ? "已完成" : "进行中") +
                            "\n" +
                            "任务状态: " + taskStatus;

            log.info("解析结果 -> \n{}", result);

            return result;
        }
    }

}
