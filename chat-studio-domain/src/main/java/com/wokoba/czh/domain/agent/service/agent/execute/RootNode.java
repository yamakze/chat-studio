package com.wokoba.czh.domain.agent.service.agent.execute;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.AgentExecuteContext;
import com.wokoba.czh.domain.agent.model.entity.AiAgentClientFlowConfigVO;
import com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity;
import com.wokoba.czh.domain.agent.model.valobj.AgentContext;
import com.wokoba.czh.domain.agent.service.agent.AbstractAgentExecuteSupport;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("executeRootNode")
public class RootNode extends AbstractAgentExecuteSupport {

    @Resource
    private Step1AnalyzerNode step1AnalyzerNode;
    @Resource
    private IChatRepository repository;

    @Override
    protected String doApply(com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap = repository.queryAiAgentClientFlowConfig(requestParameter.getAiAgentId());

        // 客户端对话组
        dynamicContext.setAiAgentClientFlowConfigVOMap(aiAgentClientFlowConfigVOMap);
        // 上下文信息
        dynamicContext.setExecutionHistory(new StringBuilder());
        // 当前任务信息
        dynamicContext.setCurrentTask(requestParameter.getMessage());
        // 最大任务步骤
        dynamicContext.setMaxStep(requestParameter.getMaxStep());

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity, AgentExecuteContext, String> get(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws Exception {
        return null;
    }
}
