package com.wokoba.czh.domain.agent.service.armory.node;

import com.alibaba.fastjson.JSON;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.model.entity.AiClientModelEntity;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.armory.AbstractArmorySupport;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class AiClientModelNode extends AbstractArmorySupport {

    @Resource
    private AiClientNode aiClientNode;

    @Override
    protected String doApply(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，客户端构建节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientModelEntity> aiClientModelList = dynamicContext.getValue("aiClientModelList");

        if (aiClientModelList == null || aiClientModelList.isEmpty()) {
            log.warn("没有可用的AI客户端模型配置");
            return null;
        }

        // 遍历模型列表，为每个模型创建对应的Bean
        for (AiClientModelEntity model : aiClientModelList) {
            if (Objects.isNull(customBeanRegistrar.getBean(beanName(model.getId())))) {
                // 创建OpenAiChatModel对象
                OpenAiChatModel chatModel = createOpenAiChatModel(model);
                // 使用父类的通用注册方法
                customBeanRegistrar.registerBean(beanName(model.getId()), OpenAiChatModel.class, chatModel);
            }
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }

    @Override
    protected String beanName(Long id) {
        return Constants.BeanName.MODEL + id;
    }

    /**
     * 创建OpenAiChatModel对象
     *
     * @param model 模型配置值对象
     * @return OpenAiChatModel实例
     */
    private OpenAiChatModel createOpenAiChatModel(AiClientModelEntity model) {
        // 构建OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(model.getBaseUrl())
                .apiKey(model.getApiKey())
                .completionsPath(model.getCompletionsPath())
                .build();

        // 构建OpenAiChatModel
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model.getModelVersion())
                        .build())
                .build();
    }

}
