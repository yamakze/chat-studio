package com.wokoba.czh.domain.agent.service.armory.node;


import com.alibaba.fastjson2.JSON;
import com.wokoba.czh.domain.agent.model.entity.AiClientToolMcpEntity;
import com.wokoba.czh.domain.agent.model.entity.ChatEngineStarterEntity;
import com.wokoba.czh.domain.agent.service.armory.AbstractArmorySupport;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.framework.tree.IStrategyHandler;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class AiClientToolMcpNode extends AbstractArmorySupport {

    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;


    @Override
    protected String doApply(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，tool mcp 节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientToolMcpEntity> aiClientToolMcpList = dynamicContext.getValue("aiClientToolMcpList");
        if (aiClientToolMcpList == null || aiClientToolMcpList.isEmpty()) {
            log.warn("没有可用的AI客户端工具配置 MCP");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientToolMcpEntity mcpVO : aiClientToolMcpList) {
            // 创建McpSyncClient对象
            McpSyncClient mcpSyncClient = createMcpSyncClient(mcpVO);
            // 使用父类的通用注册方法
            customBeanRegistrar.registerBean(beanName(mcpVO.getId()), McpSyncClient.class, mcpSyncClient);
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public IStrategyHandler<ChatEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ChatEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientAdvisorNode;
    }

    @Override
    protected String beanName(Long id) {
        return Constants.BeanName.MCP_TOOL + id;
    }

    protected McpSyncClient createMcpSyncClient(AiClientToolMcpEntity aiClientToolMcpEntity) {
        String transportType = aiClientToolMcpEntity.getTransportType();

        return switch (transportType) {
            case "sse" -> createSseMcpSyncClient(aiClientToolMcpEntity);
            case "stdio" -> createStdioMcpSyncClient(aiClientToolMcpEntity);
            default -> throw new RuntimeException("Unsupported transport type: " + transportType);
        };
    }

    private McpSyncClient createSseMcpSyncClient(AiClientToolMcpEntity aiClientToolMcpEntity) {
        // 获取 SSE 配置
        AiClientToolMcpEntity.TransportConfigSse transportConfigSse = aiClientToolMcpEntity.getTransportConfigSse();
        if (transportConfigSse == null || transportConfigSse.getSseConfigs().isEmpty()) {
            throw new IllegalArgumentException("Invalid SSE configuration");
        }
        Map<String, AiClientToolMcpEntity.TransportConfigSse.SseConfig> sseConfigMap = transportConfigSse.getSseConfigs();
        AiClientToolMcpEntity.TransportConfigSse.SseConfig sseConfig = sseConfigMap.get(aiClientToolMcpEntity.getMcpName());
        // 创建 HttpClientSseClientTransport 并初始化 McpSyncClient
        McpSyncClient mcpSyncClient = McpClient.sync(new HttpClientSseClientTransport(sseConfig.getBaseUri()))
                .requestTimeout(Duration.ofMinutes(aiClientToolMcpEntity.getRequestTimeout()))
                .build();

        var initSse = mcpSyncClient.initialize();
        log.info("Tool SSE MCP Initialized: {}", initSse);

        return mcpSyncClient;
    }

    private McpSyncClient createStdioMcpSyncClient(AiClientToolMcpEntity aiClientToolMcpEntity) {
        // 获取 Stdio 配置
        AiClientToolMcpEntity.TransportConfigStdio transportConfigStdio = aiClientToolMcpEntity.getTransportConfigStdio();
        if (transportConfigStdio == null || transportConfigStdio.getStdio().isEmpty()) {
            throw new IllegalArgumentException("Invalid Stdio configuration");
        }

        // 获取指定名称的 Stdio 配置
        Map<String, AiClientToolMcpEntity.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
        AiClientToolMcpEntity.TransportConfigStdio.Stdio stdio = stdioMap.get(aiClientToolMcpEntity.getMcpName());
        if (stdio == null) {
            throw new IllegalArgumentException("No Stdio configuration found for name: " + aiClientToolMcpEntity.getMcpName());
        }

        // 构建 ServerParameters 并初始化 McpSyncClient
        var stdioParams = ServerParameters.builder(stdio.getCommand())
                .args(stdio.getArgs())
                .env(stdio.getEvn())
                .build();

        McpSyncClient mcpSyncClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(aiClientToolMcpEntity.getRequestTimeout()))
                .build();

        var initStdio = mcpSyncClient.initialize();
        log.info("Tool Stdio MCP Initialized: {}", initStdio);

        return mcpSyncClient;
    }

}
