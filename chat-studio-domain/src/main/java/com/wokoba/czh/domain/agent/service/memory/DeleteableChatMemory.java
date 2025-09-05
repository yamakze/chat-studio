package com.wokoba.czh.domain.agent.service.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Optional;

public interface DeleteableChatMemory extends ChatMemory {
    Optional<Message> removeLastMessageByType(String conversationId, MessageType messageType);

    void removeLastUserAndAssistantMessages(String conversationId);
}
