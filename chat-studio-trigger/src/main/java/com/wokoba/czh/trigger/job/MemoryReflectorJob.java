package com.wokoba.czh.trigger.job;

import com.wokoba.czh.domain.agent.model.valobj.MemoryMetadataVO;
import com.wokoba.czh.domain.agent.service.memory.reflector.MemoryReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 负责记忆的反思与再巩固。
 * 这是一个定时任务，定期回顾近期巩固的记忆，提炼出更高层次的洞见或发现矛盾，
 * 并将这些新见解存回记忆库。
 */
@Service
public class MemoryReflectorJob {

    private static final Logger logger = LoggerFactory.getLogger(MemoryReflectionService.class);

    private final MemoryReflectionService memoryReflectionService;

    // --- 从 application.yml 注入配置 ---
    @Value("${memory.enabled:true}")
    private boolean reflectionEnabled;

    @Value("${memory.reflection.look-back-days:7}")
    private int lookBackDays;

    @Value("${memory.reflection.trigger-threshold:5}")
    private int triggerThreshold;

    @Value("${memory.reflection.batch-size:50}")
    private int batchSize;

    public MemoryReflectorJob(MemoryReflectionService memoryReflectionService) {
        this.memoryReflectionService = memoryReflectionService;
    }


    @Scheduled(cron = "${memory.reflection.schedule:0 0 4 * * ?}")
    public void reflectOnRecentMemories() {
        if (!reflectionEnabled) {
            logger.info("记忆反思功能已禁用，跳过执行。");
            return;
        }

        logger.info("开始执行定时记忆反思任务...");

        List<Document> recentMemories = memoryReflectionService.fetchRecentConsolidatedMemories(lookBackDays, batchSize);
        if (recentMemories.size() < triggerThreshold) {
            logger.info("新记忆数量不足：当前 {} 条，低于触发阈值 {}，跳过反思。", recentMemories.size(), triggerThreshold);
            return;
        }

        Map<Object, List<Document>> memoriesByClient = recentMemories.stream()
                .filter(doc -> doc.getMetadata().containsKey(MemoryMetadataVO.CLIENT_ID))
                .collect(Collectors.groupingBy(doc -> doc.getMetadata().get(MemoryMetadataVO.CLIENT_ID)));

        memoriesByClient.forEach(memoryReflectionService::reflectOnMemories);

        logger.info("本轮记忆反思任务完成。");
    }
}