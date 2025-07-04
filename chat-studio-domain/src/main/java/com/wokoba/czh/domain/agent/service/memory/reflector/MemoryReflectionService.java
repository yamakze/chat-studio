package com.wokoba.czh.domain.agent.service.memory.reflector;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.wokoba.czh.domain.agent.model.valobj.MemoryMetadataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemoryReflectionService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final Clock clock;
    private final static String promptTemplate = """
            You are a highly intelligent analyst AI. Your task is to review a list of raw facts about a user
            and synthesize them into higher-level insights.
            
            Perform the following actions:
            1. Merge related facts into a more general, insightful summary (e.g., "User bought milk" and "User likes cereal" might become "User likely eats cereal for breakfast."). Label this as 'SYNTHESIS'.
            2. Identify any direct or strong contradictions in the facts (e.g., "User loves dogs" vs. "User is allergic to dogs"). Label this as 'CONTRADICTION'.
            3. Ignore trivial or unrelated facts.
            
            Raw Facts to Review:
            ---
            {facts}
            ---
            """;

    public MemoryReflectionService(VectorStore vectorStore, ChatClient.Builder clientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = clientBuilder.build();
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * 获取指定天数内生成的 CONSOLIDATED_FACT 类型的长期记忆。
     */
    public List<Document> fetchRecentConsolidatedMemories(Integer lookBackDays, Integer batchSize) {
        Instant cutoff = clock.instant().minus(lookBackDays, ChronoUnit.DAYS);
        String filter = String.format("%s == '%s' && %s > '%s'",
                MemoryMetadataVO.MEMORY_TYPE, MemoryMetadataVO.TYPE_CONSOLIDATED,
                MemoryMetadataVO.TIMESTAMP, cutoff.toString());

        log.info("正在拉取过去 {} 天内的记忆，Filter 表达式：[{}]", lookBackDays, filter);

        SearchRequest request = SearchRequest.builder()
                .query("*")
                .topK(batchSize)
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(request);
    }


    public List<Document> fetchSynthesisMemories(Object clientId) {
        String filter = String.format("%s == '%s'",
                MemoryMetadataVO.MEMORY_TYPE, MemoryMetadataVO.TYPE_CONSOLIDATED);

        log.info("正在拉取clientId {} 的SYNTHESIS记忆，Filter 表达式：[{}]", clientId, filter);

        SearchRequest request = SearchRequest.builder()
                .query("*")
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 针对单个客户端的记忆执行“反思总结”操作。
     */
    public void reflectOnMemories(Object clientId, List<Document> memories) {
        log.info("开始对客户端 [{}] 的 {} 条记忆进行反思分析...", clientId, memories.size());
        List<Document> SynthesisMemories = this.fetchSynthesisMemories(clientId);
        memories.addAll(SynthesisMemories);

        String factsToReview = memories.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n- ", "\n- ", "\n"));


        Prompt prompt = new PromptTemplate(promptTemplate)
                .create(Map.of("facts", factsToReview));

        try {
            List<ReflectedMemory> insights = chatClient.prompt(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });

            if (insights == null || insights.isEmpty()) {
                log.info("客户端 [{}] 的记忆未生成任何新洞察。", clientId);
                return;
            }

            log.info("客户端 [{}] 的反思生成了 {} 条新记忆。", clientId, insights.size());

            List<Document> newDocs = createNewDocuments(insights, clientId);

            vectorStore.add(newDocs);
            vectorStore.delete(memories.stream().map(Document::getId).toList());

        } catch (Exception e) {
            log.error("客户端 [{}] 的反思过程中发生异常：{}", clientId, e.getMessage(), e);
        }
    }

    /**
     * 创建新的文档
     *
     * @param insights 反思结果列表
     * @param clientId 客户端ID
     * @return 新的文档列表
     */
    private List<Document> createNewDocuments(List<ReflectedMemory> insights, Object clientId) {
        return insights.stream().map(mem -> {
            Map<String, Object> metadata = Map.of(
                    MemoryMetadataVO.CLIENT_ID, clientId,
                    MemoryMetadataVO.MEMORY_TYPE, mem.type.equalsIgnoreCase(MemoryMetadataVO.TYPE_CONTRADICTION) ? MemoryMetadataVO.TYPE_CONTRADICTION : MemoryMetadataVO.TYPE_SYNTHESIS,
                    MemoryMetadataVO.TIMESTAMP, clock.instant().toString()
            );
            return new Document(mem.content, metadata);
        }).toList();
    }


    /**
     * 用于接收 LLM 输出的结构化“反思记忆”实体。
     *
     * @param content 反思后的记忆内容（总结或矛盾）
     * @param type    记忆类型：SYNTHESIS（总结）或 CONTRADICTION（矛盾）
     *                //     * @param sourceFacts 来源的原始事实列表
     */
    public record ReflectedMemory(
            @JsonPropertyDescription("The synthesized new memory or the identified contradiction.") String content,
            @JsonPropertyDescription("The type of the new memory, either 'SYNTHESIS' or 'CONTRADICTION'.") String type
//            @JsonPropertyDescription("An array of original fact strings that contributed to this new memory.") List<String> sourceFacts
    ) {
    }

}
