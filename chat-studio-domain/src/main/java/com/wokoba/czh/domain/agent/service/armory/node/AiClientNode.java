package com.wokoba.czh.domain.agent.service.armory.node;


import com.alibaba.fastjson2.JSON;
import com.wokoba.czh.domain.agent.model.entity.AiClientMateriel;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.armory.AbstractArmorySupport;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AiClientNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，对话模型节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientMateriel> aiClientVOList = dynamicContext.getValue("aiClientList");

        for (AiClientMateriel clientMateriel : aiClientVOList) {
            // 1. chatModel
            OpenAiChatModel chatModel = customBeanRegistrar.getBean(clientMateriel.getModelBeanName());

            // 2. ToolCallbackProvider
            List<McpSyncClient> mcpSyncClients = new ArrayList<>();
            List<String> mcpBeanNameList = clientMateriel.getMcpBeanNameList();
            for (String mcpBeanName : mcpBeanNameList) {
                mcpSyncClients.add(customBeanRegistrar.getBean(mcpBeanName));
            }

            // 3. Advisor
            List<Advisor> advisors = new ArrayList<>();
            List<String> advisorBeanNameList = clientMateriel.getAdvisorBeanNameList();
            for (String advisorBeanName : advisorBeanNameList) {
                advisors.add(customBeanRegistrar.getBean(advisorBeanName));
            }
//            advisors.addAll(List.of(new HistoryRecordAdvisor()));
            // 4. 构建对话客户端
            ChatClient client = ChatClient.builder(chatModel)
                    .defaultSystem(clientMateriel.getSystemPromptContent())
                    .defaultOptions(clientMateriel.getOptions().buildOpenAiOptions())
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients.toArray(new McpSyncClient[]{})))
                    .defaultAdvisors(advisors.toArray(new Advisor[]{}))
                    .build();

            String clientBeanName = beanName(clientMateriel.getClientId());
            customBeanRegistrar.clearBean(clientBeanName);
            customBeanRegistrar.registerBean(clientBeanName, ChatClient.class, client);
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    protected String beanName(Long id) {
        return Constants.BeanName.CLIENT + id;
    }

    @Override
    public IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
