package com.wokoba.czh.domain.agent.service.agent;

import com.alibaba.fastjson.JSON;
import com.wokoba.czh.domain.agent.model.entity.AgentExecuteContext;
import com.wokoba.czh.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.wokoba.czh.domain.agent.model.entity.ExecuteCommandEntity;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.framework.tree.AbstractMultiThreadStrategyRouter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractAgentExecuteSupport extends AbstractMultiThreadStrategyRouter<ExecuteCommandEntity, AgentExecuteContext, String> {
    @Resource
    private ApplicationContext applicationContext;

    private final Logger log = LoggerFactory.getLogger(AbstractAgentExecuteSupport.class);


    @Override
    protected void multiThread(ExecuteCommandEntity requestParameter, AgentExecuteContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 缺省..
    }

    protected ChatClient getChatClientByClientId(Long clientId) {
        return applicationContext.getBean(Constants.BeanName.CLIENT + clientId, ChatClient.class);
    }

    /**
     * 通用的SSE结果发送方法
     * @param dynamicContext 动态上下文
     * @param result 要发送的结果实体
     */
    protected void sendSseResult(AgentExecuteContext dynamicContext,
                                 AutoAgentExecuteResultEntity result) {
        try {
            ResponseBodyEmitter emitter = dynamicContext.getValue("emitter");
            if (emitter != null) {
                // 发送SSE格式的数据
                String sseData = "data: " + JSON.toJSONString(result) + "\n\n";
                emitter.send(sseData);
            }
        } catch (IOException e) {
            log.error("发送SSE结果失败：{}", e.getMessage(), e);
        }
    }
}
