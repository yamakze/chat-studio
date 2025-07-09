package com.wokoba.czh.domain.agent.service.armory.node;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wokoba.czh.domain.agent.model.entity.AiClientAdvisorEntity;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.AttachmentProcessor;
import com.wokoba.czh.domain.agent.service.advisor.ChatContextCorrectionAdvisor;
import com.wokoba.czh.domain.agent.service.advisor.CustomMediaAdvisor;
import com.wokoba.czh.domain.agent.service.armory.AbstractArmorySupport;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.domain.agent.service.memory.RetrievableChatMemory;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AiClientAdvisorNode extends AbstractArmorySupport {

    @Resource
    private AiClientModelNode aiClientModelNode;

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private AttachmentProcessor attachmentProcessor;

    @Resource
    private RetrievableChatMemory retrievableChatMemory;

    @Override
    protected String doApply(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，advisor 顾问节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientAdvisorEntity> aiClientAdvisorList = dynamicContext.getValue("aiClientAdvisorList");
        if (aiClientAdvisorList == null || aiClientAdvisorList.isEmpty()) {
            log.warn("没有可用的AI客户端顾问（advisor）配置");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientAdvisorEntity aiClientAdvisorEntity : aiClientAdvisorList) {
            if (Objects.isNull(customBeanRegistrar.getBean(beanName(aiClientAdvisorEntity.getId())))) {
                // 构建顾问访问对象
                Advisor advisor = createAdvisor(aiClientAdvisorEntity);
                // 注册Bean对象
                customBeanRegistrar.registerBean(beanName(aiClientAdvisorEntity.getId()), Advisor.class, advisor);
            }
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }

    @Override
    protected String beanName(Long id) {
        return Constants.BeanName.ADVISOR + id;
    }

    private Advisor createAdvisor(AiClientAdvisorEntity aiClientAdvisorEntity) {
        String advisorType = aiClientAdvisorEntity.getAdvisorType();
        JSONObject extraParams = aiClientAdvisorEntity.getExtraParams();
        switch (advisorType) {
            case "ChatMemory" -> {
                retrievableChatMemory.mutate(extraParams.getIntValue("maxMessages"), extraParams.getIntValue("retrievableK"));
                return ChatContextCorrectionAdvisor
                        .builder()
                        .memory(retrievableChatMemory)
                        .build();
            }
            case "RagAnswer" -> {
                //文档检索
                return RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(pgVectorStore)
                                .topK(extraParams.getIntValue("topK"))
                                .similarityThreshold(extraParams.getDoubleValue("similarityThreshold"))
                                .filterExpression(() -> (new FilterExpressionTextParser()).parse(extraParams.getString("filterExpression")))
                                .build())
                        .queryAugmenter(ContextualQueryAugmenter.builder()
                                .allowEmptyContext(extraParams.getBoolean("allowEmptyContext"))
                                .build())
                        .build();

            }
            case "CustomMedia" -> {
                String filePattern = aiClientAdvisorEntity.getExtraParams().getString("filePattern");
                return filePattern == null ? new CustomMediaAdvisor(attachmentProcessor) : new CustomMediaAdvisor(attachmentProcessor, filePattern);
            }

        }
        throw new RuntimeException("err! advisorType " + advisorType + " not exist!");
    }

}
