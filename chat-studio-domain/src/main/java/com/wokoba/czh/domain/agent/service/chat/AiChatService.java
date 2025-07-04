package com.wokoba.czh.domain.agent.service.chat;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.AiChatRequestEntity;
import com.wokoba.czh.domain.agent.service.IAiChatService;
import com.wokoba.czh.domain.agent.service.advisor.ChatContextCorrectionAdvisor;
import com.wokoba.czh.domain.agent.service.advisor.CustomMediaAdvisor;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatService implements IAiChatService {

    private final DefaultArmoryStrategyFactory strategyFactory;

    private final IChatRepository repository;


    public AiChatService(DefaultArmoryStrategyFactory strategyFactory, IChatRepository repository) {
        this.strategyFactory = strategyFactory;
        this.repository = repository;
    }


    @Override
    public ChatResponse aiChat(AiChatRequestEntity request) {
        return buildPrompt(strategyFactory.chatClient(request.getClientId()), request).call().chatResponse();
    }


    @Override
    public Flux<ChatResponse> aiChatStream(AiChatRequestEntity request) {
        return buildPrompt(strategyFactory.chatClient(request.getClientId()), request).stream().chatResponse();
    }

    private ChatClient.ChatClientRequestSpec buildPrompt(ChatClient client, AiChatRequestEntity req) {
        ChatClient.ChatClientRequestSpec spec = client.prompt().user(req.getUserMessage());
        applyAdvisors(spec, req);
        return spec;
    }

    private void applyAdvisors(ChatClient.ChatClientRequestSpec spec, AiChatRequestEntity req) {
        // 记忆上下文配置
        spec.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, req.getConversationId())
                .param(ChatContextCorrectionAdvisor.RETRY_ACTION_KEY, req.getRetryAction()));
        //解析附件上下文
        spec.advisors(a -> a.param(CustomMediaAdvisor.CLIENT_ID_KEY, req.getClientId()));
        // RAG 知识库配置
        if (req.getRagId() != null) {
            String tag = repository.queryRagKnowledgeTag(req.getRagId());
            spec.advisors(a -> a.param(
                    VectorStoreDocumentRetriever.FILTER_EXPRESSION,
                    String.format("knowledge == '%s'", tag)));
        }
    }


}


