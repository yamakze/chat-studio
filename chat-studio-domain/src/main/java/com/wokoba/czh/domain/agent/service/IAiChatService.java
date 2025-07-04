package com.wokoba.czh.domain.agent.service;

import com.wokoba.czh.domain.agent.model.entity.AiChatRequestEntity;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;


public interface IAiChatService {

    /**
     * 智能体对话
     */
    ChatResponse aiChat(AiChatRequestEntity request);


    Flux<ChatResponse> aiChatStream(AiChatRequestEntity request);

}