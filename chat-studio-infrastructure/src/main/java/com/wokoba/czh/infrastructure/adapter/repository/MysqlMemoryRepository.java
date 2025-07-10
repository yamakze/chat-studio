package com.wokoba.czh.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.infrastructure.dao.ChatMemoryDao;
import com.wokoba.czh.infrastructure.dao.po.SpringAiChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
//@Repository
//TODO 短期记忆暂时存储在浏览器
public class MysqlMemoryRepository implements ChatMemoryRepository {
    @Resource
    private ChatMemoryDao chatMemoryDao;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<SpringAiChatMemory> springAiChatMemories = chatMemoryDao.selectList(Wrappers.lambdaQuery(SpringAiChatMemory.class).eq(SpringAiChatMemory::getConversationId, conversationId));
        return springAiChatMemories.stream().map(this::convertToMessage).toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        List<SpringAiChatMemory> springAiChatMemories = messages.stream().map(message -> this.convertToAiChatMemory(conversationId, message)).toList();
        chatMemoryDao.delete(Wrappers.lambdaQuery(SpringAiChatMemory.class).eq(SpringAiChatMemory::getConversationId, conversationId));
        chatMemoryDao.insertBatch(springAiChatMemories);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        chatMemoryDao.delete(Wrappers.lambdaQuery(SpringAiChatMemory.class).eq(SpringAiChatMemory::getConversationId, conversationId));
    }

    private SpringAiChatMemory convertToAiChatMemory(String conversationId, Message message) {
        if (conversationId == null) {
            return null;
        }
        Map<String, Object> metadata = message.getMetadata();
        SpringAiChatMemory springAiChatMemory = new SpringAiChatMemory();
        springAiChatMemory.setConversationId(conversationId);
        springAiChatMemory.setContent(message.getText());
        springAiChatMemory.setType(message.getMessageType().getValue());
        springAiChatMemory.setStatus((String) metadata.getOrDefault("status", "created"));
        springAiChatMemory.setTimestamp((LocalDateTime) metadata.getOrDefault("timestamp", LocalDateTime.now()));
        return springAiChatMemory;
    }

    private Message convertToMessage(SpringAiChatMemory springAiChatMemory) {
        if (springAiChatMemory == null) {
            return null;
        }
        String messageType = springAiChatMemory.getType();
        return switch (messageType.toLowerCase()) {
            case "user" -> UserMessage.builder()
                    .text(springAiChatMemory.getContent())
                    .metadata(Map.of("status", springAiChatMemory.getStatus(),
                            "timestamp", springAiChatMemory.getTimestamp()))
                    .build();
            case "assistant" -> new AssistantMessage(springAiChatMemory.getContent(),
                    Map.of("status", springAiChatMemory.getStatus(),
                            "timestamp", springAiChatMemory.getTimestamp()));
            case "system" -> new SystemMessage(springAiChatMemory.getContent());
            default -> {
                log.warn("未知的消息类型:{}", messageType);
                yield null;
            }
        };
    }
}
