package com.wokoba.czh.domain.agent.service.agent.tools;

import com.wokoba.czh.domain.agent.model.valobj.AgentContext;
import com.wokoba.czh.domain.agent.model.valobj.AgentStatus;

public class EndTaskTool {

    private final AgentContext context;

    public static final String END_TASK_DESC = "MUST be called When you can't continue with the task. This is the final step to conclude the entire process and provide the answer to the user.";

    public EndTaskTool(AgentContext context) {
        this.context = context;
    }

    public void endTask() {
        context.setStatus(AgentStatus.COMPLETED); // 设置任务状态为完成
    }
}