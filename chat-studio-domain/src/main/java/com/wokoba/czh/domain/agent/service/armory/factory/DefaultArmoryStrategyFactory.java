package com.wokoba.czh.domain.agent.service.armory.factory;

import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.armory.node.RootNode;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import lombok.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class DefaultArmoryStrategyFactory  {


    private final ApplicationContext applicationContext;

    private final RootNode rootNode;



    public DefaultArmoryStrategyFactory(ApplicationContext applicationContext, RootNode rootNode) {
        this.applicationContext = applicationContext;
        this.rootNode = rootNode;
    }

    public IStrategyHandler<ChatEngineStarterEntity, DynamicContext, String> strategyHandler() {
        return rootNode;
    }

    @SneakyThrows
    public ChatClient chatClient(Long clientId) {
        String beanName = "ChatClient_" + clientId;
        return applicationContext.getBean(beanName, ChatClient.class);
    }



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private int level;

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }

}
