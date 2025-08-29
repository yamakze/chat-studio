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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Step3QualitySupervisorNode extends AbstractAgentExecuteSupport {

    @Resource
    private Step4LogExecutionSummaryNode step4LogExecutionSummaryNode;
//    @Resource
    private Step1AnalyzerNode step1AnalyzerNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        // 第三阶段：质量监督
        log.info("\n🔍 阶段3: 质量监督检查");

        // 从动态上下文中获取执行结果
        String executionResult = dynamicContext.getValue("executionResult");
        if (executionResult == null || executionResult.trim().isEmpty()) {
            log.warn("⚠️ 执行结果为空，跳过质量监督");
            return "质量监督跳过";
        }

        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getAiAgentClientFlowConfigVOMap().get(AiClientTypeEnumVO.QUALITY_SUPERVISOR_CLIENT.getCode());

        String supervisionPrompt = String.format(aiAgentClientFlowConfigVO.getStepPrompt(), requestParameter.getMessage(), executionResult);

        // 获取对话客户端
        ChatClient chatClient = getChatClientByClientId(aiAgentClientFlowConfigVO.getClientId());

        SupervisionResult supervisionResult = chatClient
                .prompt(supervisionPrompt)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, requestParameter.getSessionId()))
                .call().entity(SupervisionResult.class);

        assert supervisionResult != null;
        String parseToString = supervisionResult.parseToString();
        sendSupervisionResult(dynamicContext, parseToString, requestParameter.getSessionId());

        // 将监督结果保存到动态上下文中
        dynamicContext.setValue("supervisionResult", supervisionResult);

        // 根据监督结果决定是否需要重新执行
        if (supervisionResult.pass.equalsIgnoreCase("是否通过: FAIL")) {
            log.info("❌ 质量检查未通过，需要重新执行");
            dynamicContext.setCurrentTask("根据质量监督的建议重新执行任务");
        } else if (supervisionResult.pass.equalsIgnoreCase("是否通过: OPTIMIZE")) {
            log.info("🔧 质量检查建议优化，继续改进");
            dynamicContext.setCurrentTask("根据质量监督的建议优化执行结果");
        } else {
            log.info("✅ 质量检查通过");
            dynamicContext.setCompleted(true);
        }

        // 更新执行历史
        String stepSummary = String.format("""
                        === 第 %d 步完整记录 ===
                        【分析阶段】%s
                        【执行阶段】%s
                        【监督阶段】%s
                        """, dynamicContext.getStep(),
                dynamicContext.getValue("analysisResult"),
                executionResult,
                supervisionResult);

        dynamicContext.getExecutionHistory().append(stepSummary);

        // 增加步骤计数
        dynamicContext.setStep(dynamicContext.getStep() + 1);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ExecuteCommandEntity, AgentExecuteContext, String> get(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return step4LogExecutionSummaryNode;
        }
        // 否则返回到Step1AnalyzerNode进行下一轮分析
        return step1AnalyzerNode;
    }

    /**
     * 发送监督结果到流式输出
     */
    private void sendSupervisionResult(AgentExecuteContext dynamicContext,
                                       String supervisionResult, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSupervisionResult(
                dynamicContext.getStep(), supervisionResult, sessionId);
        sendSseResult(dynamicContext, result);
    }


    private record SupervisionResult(
            @JsonProperty("执行结果与用户原始需求的匹配程度分析") String assessment,   // 质量评估
            @JsonProperty("发现的问题和不足") String issues,       // 问题识别
            @JsonProperty("具体的改进建议") String suggestions,  // 改进建议
            @JsonProperty("是否通过: [PASS/FAIL/OPTIMIZE]") String pass          // 是否通过 (PASS / FAIL / OPTIMIZE)
    ) {
        String parseToString() {
            Logger log = LoggerFactory.getLogger(SupervisionResult.class);

            String result = "质量评估: " + (assessment == null ? "无" : assessment) + "\n" +
                            "问题识别: " + (issues == null ? "无" : issues) + "\n" +
                            "改进建议: " + (suggestions == null ? "无" : suggestions) + "\n" +
                            "是否通过: " + (pass == null ? "未知" : pass);


            log.info("\n🔍 监督结果解析 -> \n{}", result);
            return result;
        }
    }


}
