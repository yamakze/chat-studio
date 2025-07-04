package com.wokoba.czh.domain.agent.service.armory.node;


import com.alibaba.fastjson.JSON;
import com.wokoba.czh.domain.agent.model.entity.*;
import com.wokoba.czh.domain.agent.service.armory.AbstractArmorySupport;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class RootNode extends AbstractArmorySupport {

    @Resource
    private AiClientToolMcpNode aiClientToolMcpNode;

    @Override
    protected void multiThread(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        CompletableFuture<List<AiClientModelEntity>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", requestParameter.getClientIdList());
            return repository.queryAiClientModelVOListByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientToolMcpEntity>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", requestParameter.getClientIdList());
            return repository.queryAiClientToolMcpVOListByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientAdvisorEntity>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", requestParameter.getClientIdList());
            return repository.queryAdvisorConfigByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientMateriel>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", requestParameter.getClientIdList());
            return repository.queryAiClientByClientIds(requestParameter.getClientIdList());
        }, threadPoolExecutor);

        CompletableFuture.allOf(aiClientToolMcpListFuture, aiClientAdvisorListFuture, aiClientListFuture)
                .thenRun(() -> {
                    dynamicContext.setValue("aiClientModelList", aiClientModelListFuture.join());
                    dynamicContext.setValue("aiClientToolMcpList", aiClientToolMcpListFuture.join());
                    dynamicContext.setValue("aiClientAdvisorList", aiClientAdvisorListFuture.join());
                    dynamicContext.setValue("aiClientList", aiClientListFuture.join());
                }).join();
    }

    @Override
    protected String doApply(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，数据加载节点 {}", JSON.toJSONString(requestParameter));
        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientToolMcpNode;
    }

}
