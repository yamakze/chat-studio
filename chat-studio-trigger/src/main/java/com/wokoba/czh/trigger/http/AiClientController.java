package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.api.dto.AiClientConfigRequestDTO;
import com.wokoba.czh.api.dto.AiClientResponseDTO;
import com.wokoba.czh.domain.agent.model.entity.AiClientMateriel;
import com.wokoba.czh.domain.agent.model.valobj.AiClientOptionsVO;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.armory.AiAgentPreheatService;
import com.wokoba.czh.domain.agent.service.client.AiClientService;
import com.wokoba.czh.infrastructure.dao.AiClientAdvisorConfigDao;
import com.wokoba.czh.infrastructure.dao.AiClientDao;
import com.wokoba.czh.infrastructure.dao.AiClientToolConfigDao;
import com.wokoba.czh.infrastructure.dao.po.AiClient;
import com.wokoba.czh.infrastructure.dao.po.AiClientAdvisorConfig;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolConfig;
import com.wokoba.czh.types.common.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/client")
@Slf4j
public class AiClientController {
    @Autowired
    private AiClientDao aiClientDao;
    @Autowired
    private AiClientAdvisorConfigDao aiClientAdvisorConfigDao;
    @Autowired
    private AiClientToolConfigDao aiClientToolConfigDao;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AiClientService aiClientService;

    /**
     * 更新客户端基础信息
     */
    @PutMapping
    public ResponseEntity<String> updateAiClient(@RequestParam @NotNull Long clientId,
                                                 @RequestParam @NotBlank String name,
                                                 @RequestParam @NotBlank String description,
                                                 @RequestParam(required = false) Integer status) {
        try {
            log.info("更新client基础信息开始 clientId:{}", clientId);
            aiClientDao.update(Wrappers.lambdaUpdate(AiClient.class)
                    .eq(AiClient::getId, clientId)
                    .set(AiClient::getClientName, name)
                    .set(AiClient::getDescription, description)
                    .set(Objects.nonNull(status), AiClient::getStatus, status));

            return ResponseEntity.ok("更新client成功");
        } catch (Exception e) {
            log.error("更新client失败 clientId:{}", clientId, e);
            return ResponseEntity.badRequest().body("Error updating client " + clientId);
        }
    }

    /**
     * 更新客户端配置
     */
    @PutMapping("/config")
    public ResponseEntity<String> updateAiClientConfig(@RequestBody @Validated AiClientConfigRequestDTO requestDTO) {
        Long clientId = requestDTO.getClientId();
        try {

            log.info("开始更新AI客户端配置，clientId:{}", clientId);
            aiClientService.changeAiClientConfig(AiClientMateriel.builder()
                    .advisorIdList(requestDTO.getAdvisorIds())
                    .clientId(clientId)
                    .systemPromptId(requestDTO.getSystemPromptId())
                    .mcpIdList(requestDTO.getMcpIds())
                    .modelId(requestDTO.getModelId())
                    .options(objectMapper.convertValue(requestDTO.getOptions(), AiClientOptionsVO.class))
                    .build());
            return ResponseEntity.ok("更新AI客户端配置成功");
        } catch (Exception e) {
            log.error("更新AI客户端配置失败 clientId:{}", clientId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询全部客户端
     */
    @GetMapping
    public ResponseEntity<List<AiClient>> getAllAiClients() {
        return ResponseEntity.ok(aiClientDao.selectList(Wrappers.lambdaQuery(AiClient.class).orderByDesc(AiClient::getCreateTime)));
    }

    /**
     * 查询有效客户端
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiClient>> getValidAiClients() {
        return ResponseEntity.ok(aiClientDao.selectList(Wrappers.lambdaQuery(AiClient.class).eq(AiClient::getStatus, 1).orderByDesc(AiClient::getCreateTime)));
    }

    /**
     * 分页查询客户端
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiClient>> getAiClientPage(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {

        return ResponseEntity.ok(aiClientDao.selectList(Wrappers.lambdaQuery(AiClient.class)
                .orderByDesc(AiClient::getCreateTime)
                .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询客户端详情
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<AiClientResponseDTO> getAiClientById(@PathVariable Long clientId) {
        AiClient client = aiClientDao.selectById(clientId);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        List<Long> advisorIdList = aiClientAdvisorConfigDao.queryAdvisorIdsByClientIds(List.of(clientId));
        List<Long> mcpIdList = aiClientToolConfigDao.queryMcpIdsByClientIds(List.of(clientId));

        AiClientResponseDTO aiClientResponseDTO = new AiClientResponseDTO();
        aiClientResponseDTO.setId(client.getId());
        aiClientResponseDTO.setModelId(client.getModelId());
        aiClientResponseDTO.setOptionsJsonStr(client.getOptions());
        aiClientResponseDTO.setSystemPromptId(client.getSystemPromptId());
        aiClientResponseDTO.setClientName(client.getClientName());
        aiClientResponseDTO.setDescription(client.getDescription());
        aiClientResponseDTO.setAdvisorIds(advisorIdList);
        aiClientResponseDTO.setMcpIds(mcpIdList);
        return ResponseEntity.ok(aiClientResponseDTO);
    }

    /**
     * 创建客户端
     */
    @PostMapping
    public ResponseEntity<String> createAiClient() {
        log.info("初始化client开始");
        aiClientService.initAiClient();
        return ResponseEntity.ok("初始化client成功");
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteAiClientById(@PathVariable Long clientId) {
        log.info("删除client开始 clientId:{}", clientId);
        aiClientService.destroy(clientId);
        return ResponseEntity.ok("删除client成功");
    }
}

