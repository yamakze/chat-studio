package com.wokoba.czh.domain.agent.service.memory.analyzer;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.wokoba.czh.domain.agent.model.valobj.MemoryMetadataVO;
import com.wokoba.czh.domain.agent.service.memory.RetrievableChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Content;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MemoryConsolidationService {

    private final ChatClient llmAider;
    private final VectorStore vectorStore;
    private final RetrievableChatMemory chatMemory;

    public MemoryConsolidationService(ChatClient.Builder clientBuilder, VectorStore vectorStore, RetrievableChatMemory chatMemory) {
        this.llmAider = clientBuilder
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.8)
                        .build())
                .build();
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    public void consolidate(Long clientId, Long hoursBack) {
        List<Message> messages = chatMemory.get("chat_" + clientId,true);
        List<String> conversationTexts = filterMessagesByTime(messages, hoursBack).stream().map(Content::getText).toList();
        analyzeAndStoreMemories(clientId, conversationTexts);
    }

    public void consolidate(Long clientId) {
        consolidate(clientId, 6L); // 默认查看最近6小时内的消息
    }

    private List<Message> filterMessagesByTime(List<Message> messages, Long hoursBack) {
        final LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursBack);
        return messages.stream()
                .filter(message -> {
                    Object timestampObj = message.getMetadata().get(MemoryMetadataVO.TIMESTAMP);
                    // 增加健壮性检查
                    if (!(timestampObj instanceof LocalDateTime timestamp)) {
                        return true; // 如果没有时间戳或类型不符，可以选择保留或忽略
                    }
                    return timestamp.isAfter(cutoffTime);
                })
                .toList();
    }

    public void analyzeAndStoreMemories(Long clientId, List<String> conversationTexts) {
        String conversations = String.join("\n---\n", conversationTexts);

        if (conversations.isBlank()) return;
        String promptTemplate = """
                You are a memory analysis expert. From the following conversation transcript,
                extract key facts, user preferences, important events, or stated goals.
                For each piece of information, create a concise, context-free memory statement.
                Do not extract trivial or temporary information. Focus on long-term value.
                
                Conversation Transcript:
                ---
                {conversations}
                ---
                """;

        Prompt prompt = new PromptTemplate(promptTemplate).create(Map.of(
                "conversations", conversations
        ));


        // 调用LLM并解析输出
        List<ConsolidatedMemory> memories = llmAider
                .prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<ConsolidatedMemory>>() {
                });


        // 将新记忆存入VectorStore
        List<Document> newDocs = Objects.requireNonNull(memories).stream()
                .map(mem -> new Document(
                        mem.memory(),
                        Map.of(
                                MemoryMetadataVO.CLIENT_ID, clientId,
                                MemoryMetadataVO.IMPORT_ANCE_SCORE, mem.importance(),
                                MemoryMetadataVO.TIMESTAMP, Instant.now().toString(),
                                MemoryMetadataVO.MEMORY_TYPE, MemoryMetadataVO.TYPE_CONSOLIDATED
                        )
                )).toList();

        if (!newDocs.isEmpty()) {
            vectorStore.add(newDocs);
        }
    }

    public record ConsolidatedMemory(
            @JsonPropertyDescription("A concise statement of a single fact, preference, or event.") String memory,
            @JsonPropertyDescription("The importance of this memory on a scale from 0.0 to 1.0.") float importance
    ) {
    }
}