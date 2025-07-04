package com.wokoba.czh.domain.agent.service.armory;


import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> {

    @Resource
    protected CustomBeanRegistrar customBeanRegistrar;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Resource
    protected IChatRepository repository;

    @Override
    protected void multiThread(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        //缺省
    }

    protected String beanName(Long id) {
        // 缺省的
        return "default";
    }

}
