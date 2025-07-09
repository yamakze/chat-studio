package com.wokoba.czh.domain.agent.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.AiClientMateriel;
import com.wokoba.czh.domain.agent.model.valobj.AiClientOptionsVO;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import com.wokoba.czh.types.common.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiClientService {
    @Autowired
    private IAiAgentPreheatService aiAgentPreheatService;
    @Autowired
    private IChatRepository repository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private CustomBeanRegistrar customBeanRegistrar;
    @Autowired
    private VectorStore vectorStore;


    @SneakyThrows
    public void changeAiClientConfig(AiClientMateriel clientMateriel) {
        String optionsJsonStr = objectMapper.writeValueAsString(clientMateriel.getOptions());
        repository.updateClientConfig(clientMateriel.getClientId(), clientMateriel.getSystemPromptId(), clientMateriel.getModelId(), clientMateriel.getMcpIdList(), clientMateriel.getAdvisorIdList(), optionsJsonStr);
        aiAgentPreheatService.preheat(clientMateriel.getClientId());
    }

    @SneakyThrows
    public void initAiClient() {
        AiClientMateriel aiClientMateriel = repository.queryClientBasicMaterials();
        Long clientId = repository.initAiClient();
        repository.updateClientConfig(clientId,
                aiClientMateriel.getSystemPromptId(),
                aiClientMateriel.getModelId(),
                aiClientMateriel.getMcpIdList(),
                aiClientMateriel.getAdvisorIdList(),
                objectMapper.writeValueAsString(new AiClientOptionsVO()));
        aiAgentPreheatService.preheat(clientId);
    }

    public void destroy(Long clientId) {
        chatMemory.clear("chat_" + clientId);
        customBeanRegistrar.clearBean(Constants.BeanName.CLIENT + clientId);
        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("clientId"), new Filter.Value(clientId)));
        repository.deleteClientById(clientId);
    }
}

