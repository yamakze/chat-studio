package com.wokoba.czh.domain.agent.service.agent;


import com.wokoba.czh.domain.agent.model.valobj.AgentContext;
import com.wokoba.czh.domain.agent.service.IAiAgentService;
import com.wokoba.czh.domain.agent.service.agent.tools.EndTaskTool;
import com.wokoba.czh.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAgentService implements IAiAgentService {
    @Autowired
    protected DefaultArmoryStrategyFactory armoryStrategyFactory;

    protected List<ToolCallback> registeredTools = new ArrayList<>();

    protected final int MAX_STEPS = 20;


    @Override
    public String agentChat(Long clientId, String input) {
        AgentContext context = AgentContext.create(clientId, input, armoryStrategyFactory.chatClient(clientId));
        context.getRegisteredTools().add(buildEndTaskTool(context));
        // 1. 规划阶段
        plan(context);
        // 2. 执行阶段
        execute(context);

        return context.getResult().toString();
    }


    protected abstract void execute(AgentContext context);

    protected abstract void plan(AgentContext context);

    private ToolCallback buildEndTaskTool(AgentContext context) {
        Method endTask = ReflectionUtils.findMethod(EndTaskTool.class, "endTask");
        return MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder() // 假设EndTaskTool有一个endTask方法
                        .description(EndTaskTool.END_TASK_DESC)
                        .build())
                .toolMetadata(ToolMetadata.builder()
                        .returnDirect(true)
                        .build())
                .toolMethod(endTask)
                .toolObject(new EndTaskTool(context))
                .build();
    }
}
