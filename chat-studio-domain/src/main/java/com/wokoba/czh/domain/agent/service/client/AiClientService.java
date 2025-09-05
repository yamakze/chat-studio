package com.wokoba.czh.domain.agent.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.AiClientEntity;
import com.wokoba.czh.domain.agent.model.entity.AiClientMateriel;
import com.wokoba.czh.domain.agent.model.valobj.AiClientVO;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import com.wokoba.czh.domain.agent.service.memory.DeleteableChatMemory;
import com.wokoba.czh.types.common.Constants;
import com.wokoba.czh.types.enums.ResponseCode;
import com.wokoba.czh.types.exception.AppException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AiClientService {
    @Autowired
    private IAiAgentPreheatService aiAgentPreheatService;
    @Autowired
    private IChatRepository repository;
    @Autowired
    private DeleteableChatMemory chatMemory;
    @Autowired
    private CustomBeanRegistrar customBeanRegistrar;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private ObjectMapper objectMapper;


    @SneakyThrows
    public void changeAiClientConfig(AiClientMateriel clientMateriel) {
        repository.updateClientConfig(clientMateriel);
        aiAgentPreheatService.preheat(clientMateriel.getClientId());
    }

    @SneakyThrows
    public void initAiClient() {
        Long clientId = repository.initAiClient();
        AiClientMateriel aiClientMateriel = repository.queryClientBasicMaterials();
        aiClientMateriel.setClientId(clientId);
        repository.updateClientConfig(aiClientMateriel);
        aiAgentPreheatService.preheat(clientId);
    }

    public void destroy(Long clientId) {
        chatMemory.clear("chat_" + clientId);
        customBeanRegistrar.clearBean(Constants.BeanName.CLIENT + clientId);
        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("clientId"), new Filter.Value(clientId)));
        repository.deleteClientById(clientId);
    }

    @SneakyThrows
    public AiClientEntity getClientEntityById(Long clientId) {
        List<AiClientMateriel> aiClientMateriels = repository.queryClientMaterielByClientIds(List.of(clientId));

        AiClientMateriel aiClientMateriel = aiClientMateriels.get(0);
        AiClientVO clientVO = repository.queryClientBasicInfoById(clientId);

        return AiClientEntity.builder()
                .clientId(clientId)
                .systemPromptContent(aiClientMateriel.getSystemPromptContent())
                .systemPromptId(aiClientMateriel.getSystemPromptId())
                .clientName(clientVO.getClientName())
                .description(clientVO.getDescription())
                .status(clientVO.getStatus())
                .modelId(aiClientMateriel.getModelId())
                .modelVersion(aiClientMateriel.getModelVersion())
                .options(objectMapper.writeValueAsString(aiClientMateriel.getOptions()))
                .advisorIdList(aiClientMateriel.getAdvisorIdList())
                .mcpIdList(aiClientMateriel.getMcpIdList())
                .build();
    }
}

