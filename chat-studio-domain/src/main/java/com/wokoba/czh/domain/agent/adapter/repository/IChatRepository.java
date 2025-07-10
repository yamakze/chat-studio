package com.wokoba.czh.domain.agent.adapter.repository;

import com.wokoba.czh.domain.agent.model.entity.*;

import java.util.List;

public interface IChatRepository {
    List<AiClientModelEntity> queryAiClientModelVOListByClientIds(List<Long> clientIdList);

    List<AiClientToolMcpEntity> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList);

    List<AiClientAdvisorEntity> queryAdvisorConfigByClientIds(List<Long> clientIdList);

    List<AiClientMateriel> queryAiClientByClientIds(List<Long> clientIdList);

    List<Long> queryAiClientIds();

    String queryRagKnowledgeTag(Long ragId);

    void storeRagOrder(String name, String tag);

    void updateClientConfig(Long clientId, Long systemPromptId, Long modelId, List<Long> mcpIdList, List<Long> advisorIdList, String optionsJsonStr);

    int deleteRagOrder(Long ragId);

    AiClientMateriel queryClientBasicMaterials();

    Long initAiClient();

    List<AiTaskScheduleEntity> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

    void deleteClientById(Long clientId);

    void insertTaskExecutionRecord(Long taskId, String request, String response, Integer totalTokens, String status);
}
