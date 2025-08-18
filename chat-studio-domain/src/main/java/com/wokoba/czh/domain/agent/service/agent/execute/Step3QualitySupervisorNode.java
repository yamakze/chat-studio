package com.wokoba.czh.domain.agent.service.agent.execute;

import com.wokoba.czh.domain.agent.model.entity.AgentExecuteContext;
import com.wokoba.czh.domain.agent.model.entity.AiAgentClientFlowConfigVO;
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
public class Step3QualitySupervisorNode extends AbstractAgentExecuteSupport {

    @Resource
    private Step4LogExecutionSummaryNode step4LogExecutionSummaryNode;
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

        String supervisionResult = chatClient
                .prompt(supervisionPrompt)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, requestParameter.getSessionId()))
                .call().content();

        assert supervisionResult != null;
        parseSupervisionResult(dynamicContext, supervisionResult, requestParameter.getSessionId());
        
        // 将监督结果保存到动态上下文中
        dynamicContext.setValue("supervisionResult", supervisionResult);
        
        // 根据监督结果决定是否需要重新执行
        if (supervisionResult.contains("是否通过: FAIL")) {
            log.info("❌ 质量检查未通过，需要重新执行");
            dynamicContext.setCurrentTask("根据质量监督的建议重新执行任务");
        } else if (supervisionResult.contains("是否通过: OPTIMIZE")) {
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
        
        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return router(requestParameter, dynamicContext);
        }
        
        // 否则继续下一轮执行，返回到Step1AnalyzerNode
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
     * 解析监督结果
     */
    private void parseSupervisionResult(AgentExecuteContext dynamicContext, String supervisionResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n🔍 === 第 {} 步监督结果 ===", step);
        
        String[] lines = supervisionResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.contains("质量评估:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "assessment";
                sectionContent.setLength(0);
                log.info("\n📊 质量评估:");
                continue;
            } else if (line.contains("问题识别:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "issues";
                sectionContent.setLength(0);
                log.info("\n⚠️ 问题识别:");
                continue;
            } else if (line.contains("改进建议:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "suggestions";
                sectionContent.setLength(0);
                log.info("\n💡 改进建议:");
                continue;
            } else if (line.contains("质量评分:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "score";
                sectionContent.setLength(0);
                String score = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 质量评分: {}", score);
                sectionContent.append(score);
                continue;
            } else if (line.contains("是否通过:")) {
                // 发送前一个部分的内容
                sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "pass";
                sectionContent.setLength(0);
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("PASS")) {
                    log.info("\n✅ 检查结果: 通过");
                } else if (status.equals("FAIL")) {
                    log.info("\n❌ 检查结果: 未通过");
                } else {
                    log.info("\n🔧 检查结果: 需要优化");
                }
                sectionContent.append(status);
                continue;
            }
            
            // 收集当前部分的内容
            if (!currentSection.isEmpty()) {
                if (!sectionContent.isEmpty()) {
                    sectionContent.append("\n");
                }
                sectionContent.append(line);
            }
            
            switch (currentSection) {
                case "assessment":
                    log.info("   📋 {}", line);
                    break;
                case "issues":
                    log.info("   ⚠️ {}", line);
                    break;
                case "suggestions":
                    log.info("   💡 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
        
        // 发送最后一个部分的内容
        sendSupervisionSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
        
        // 发送完整的监督结果
        sendSupervisionResult(dynamicContext, supervisionResult, sessionId);
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
    
    /**
     * 发送监督子结果到流式输出（细粒度标识）
     */
    private void sendSupervisionSubResult(AgentExecuteContext dynamicContext,
                                        String section, String content, String sessionId) {
        // 抽取的通用判断逻辑
        if (!content.isEmpty() && !section.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createSupervisionSubResult(
                    dynamicContext.getStep(), section, content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }

}
