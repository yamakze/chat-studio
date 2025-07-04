package com.wokoba.czh.trigger.job;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.service.memory.analyzer.MemoryConsolidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MemoryConsolidationJob {
    @Autowired
    private MemoryConsolidationService memoryConsolidationService;

    @Autowired
    private IChatRepository repository;

    @Value("${memory.consolidate.look-back-hours:6}")
    private Long lookBackHours;
    @Value("${memory.enabled:true}")
    private Boolean consolidateEnabled;


    @Scheduled(cron = "${memory.consolidate.schedule:0 0 0/6 * * ?}")
    public void executeTask() {
        try {
            if (!consolidateEnabled) {
                log.info("长期记忆整合功能已禁用，跳过执行。");
                return;
            }
            log.info("开始收集记忆..");
            List<Long> clientIds = repository.queryAiClientIds();
            for (Long clientId : clientIds) {
                memoryConsolidationService.consolidate(clientId, lookBackHours);
            }
            log.info("长期记忆整合完成");
        } catch (Exception e) {
            log.error("长期记忆收集异常", e);
        }

    }
}
