package com.wokoba.czh.domain.agent.service.memory;

import lombok.Setter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RetrievableChatMemory implements ChatMemory {

    private static final int DEFAULT_MAX_MESSAGES = 20;
    private static final int DEFAULT_RETRIEVABLE_K = 10;

    private final ChatMemoryRepository chatMemoryRepository;
    private int maxMessages;
    private int retrievableK;

    private RetrievableChatMemory(ChatMemoryRepository chatMemoryRepository, int maxMessages, int retrievableK) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
        Assert.isTrue(retrievableK > 0, "retrievableK must be greater than 0");
        Assert.isTrue(maxMessages > retrievableK, "maxMessages must be greater than retrievableK");

        this.chatMemoryRepository = chatMemoryRepository;
        this.maxMessages = maxMessages;
        this.retrievableK = retrievableK;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        List<Message> memoryMessages = this.chatMemoryRepository.findByConversationId(conversationId);
        List<Message> processedMessages = this.process(memoryMessages, messages);
        this.chatMemoryRepository.saveAll(conversationId, processedMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return this.get(conversationId, false);
    }

    public List<Message> get(String conversationId, boolean isAll) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<Message> allMessages = this.chatMemoryRepository.findByConversationId(conversationId);

        // 消息总数小于或等于 k，返回所有消息。
        if (isAll || allMessages.size() <= this.retrievableK) {
            return allMessages;
        }

        // 仅返回最后“k”条消息
        return allMessages.subList(allMessages.size() - this.retrievableK, allMessages.size());
    }


    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }

    public void setRetrievableK(int retrievableK) {
        Assert.isTrue(maxMessages > retrievableK, "maxMessages must be greater than retrievableK");
        this.retrievableK = retrievableK > 0 ? retrievableK : DEFAULT_RETRIEVABLE_K;
    }

    public void setMaxMessages(int maxMessages) {
        Assert.isTrue(maxMessages > retrievableK, "maxMessages must be greater than retrievableK");
        this.maxMessages = maxMessages > 0 ? maxMessages : DEFAULT_MAX_MESSAGES;
    }

    public void mutate(int maxMessages, int retrievableK) {
        Assert.isTrue(maxMessages > retrievableK, "maxMessages must be greater than retrievableK");
        this.maxMessages = maxMessages;
        this.retrievableK = retrievableK;
    }

    /**
     * 根据现有消息处理新消息，处理系统消息并修剪至 maxMessages 数量。
     */
    private List<Message> process(List<Message> memoryMessages, List<Message> newMessages) {
        List<Message> processedMessages = new ArrayList<>();
        Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);

        boolean hasNewSystemMessage = newMessages.stream()
                .filter(SystemMessage.class::isInstance)
                .anyMatch(message -> !memoryMessagesSet.contains(message));

        // 如果有新的系统消息，旧的系统消息就会被过滤掉。
        memoryMessages.stream()
                .filter(message -> !hasNewSystemMessage || !(message instanceof SystemMessage))
                .forEach(processedMessages::add);

        processedMessages.addAll(newMessages);

        if (processedMessages.size() <= this.maxMessages) {
            return processedMessages;
        }

        //修剪最旧的非系统消息以适应 maxMessages。
        int messagesToRemove = processedMessages.size() - this.maxMessages;
        List<Message> trimmedMessages = new ArrayList<>();
        int removedCount = 0;

        for (Message message : processedMessages) {
            if (!(message instanceof SystemMessage) && removedCount < messagesToRemove) {
                removedCount++;
            } else {
                trimmedMessages.add(message);
            }
        }
        return trimmedMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ChatMemoryRepository chatMemoryRepository;
        private int maxMessages = DEFAULT_MAX_MESSAGES;
        private int retrievableK = DEFAULT_RETRIEVABLE_K;

        private Builder() {
        }

        public Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder retrievableK(int retrievableK) {
            this.retrievableK = retrievableK;
            return this;
        }

        public RetrievableChatMemory build() {
            if (this.chatMemoryRepository == null) {
                this.chatMemoryRepository = new InMemoryChatMemoryRepository();
            }

            return new RetrievableChatMemory(this.chatMemoryRepository, this.maxMessages, this.retrievableK);
        }
    }
}