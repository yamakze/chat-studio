package com.wokoba.czh.domain.agent.adapter.repository;

import com.wokoba.czh.domain.agent.model.entity.*;
import com.wokoba.czh.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.wokoba.czh.domain.agent.model.valobj.AiClientVO;

import java.util.List;
import java.util.Map;

public interface IChatRepository {
    List<AiClientModelEntity> queryAiClientModelVOListByClientIds(List<Long> clientIdList);

    List<AiClientToolMcpEntity> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList);

    List<AiClientAdvisorEntity> queryAdvisorConfigByClientIds(List<Long> clientIdList);

    List<AiClientMateriel> queryClientMaterielByClientIds(List<Long> clientIdList);

    List<Long> queryAiClientIds();

    String queryRagKnowledgeTag(Long ragId);

    void storeRagOrder(String name, String tag);

    void updateClientConfig(AiClientMateriel materiel);

    int deleteRagOrder(Long ragId);

    AiClientMateriel queryClientBasicMaterials();

    Long initAiClient();

    List<AiTaskScheduleEntity> findAllValidSchedulesWithActiveClient();

    List<Long> queryAllInvalidTaskScheduleIds();

    void deleteClientById(Long clientId);

    void insertTaskExecutionRecord(Long taskId, String request, String response, Integer totalTokens, String status);

    Map<String, AiAgentClientFlowConfigVO> queryAiAgentClientFlowConfig(String aiAgentId);

    AiClientVO queryClientBasicInfoById(Long clientId);
}
