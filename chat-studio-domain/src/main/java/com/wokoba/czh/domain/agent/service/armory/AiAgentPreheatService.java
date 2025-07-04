package com.wokoba.czh.domain.agent.service.armory;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class AiAgentPreheatService implements IAiAgentPreheatService {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;
    @Resource
    private IChatRepository repository;

    @Override
    public void preheat() throws Exception {
        List<Long> aiClientIds = repository.queryAiClientIds();
        IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();
        handler.apply(ChatEngineStarterEntity.builder()
                .clientIdList(aiClientIds)
                .build(), new DefaultArmoryStrategyFactory.DynamicContext());
    }

    @Override
    public void preheat(Long... aiClientIds) throws Exception {
        if (ArrayUtils.isEmpty(aiClientIds)) return;
        IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();
        handler.apply(ChatEngineStarterEntity.builder()
                .clientIdList(List.of(aiClientIds))
                .build(), new DefaultArmoryStrategyFactory.DynamicContext());
    }

}
