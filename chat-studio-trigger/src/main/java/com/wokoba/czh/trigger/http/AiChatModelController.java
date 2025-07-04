package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.api.dto.AiModelRequestDTO;
import com.wokoba.czh.api.dto.AiModelResponseDTO;
import com.wokoba.czh.api.group.Groups;
import com.wokoba.czh.infrastructure.adapter.port.OpenAiPort;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import com.wokoba.czh.infrastructure.dao.AiChatModelDao;
import com.wokoba.czh.infrastructure.dao.AiClientDao;
import com.wokoba.czh.infrastructure.dao.po.AiClient;
import com.wokoba.czh.infrastructure.dao.po.AiClientModel;
import com.wokoba.czh.types.common.Constants;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/model")
@Slf4j
public class AiChatModelController {
    @Autowired
    private AiChatModelDao aiChatModelDao;
    @Autowired
    private IAiAgentPreheatService aiAgentPreheatService;
    @Autowired
    private AiClientDao aiClientDao;
    @Autowired
    private CustomBeanRegistrar customBeanRegistrar;
    @Autowired
    private OpenAiPort openAiPort;

    /**
     * 查询全部模型
     */
    @GetMapping
    public ResponseEntity<List<AiClientModel>> queryModelList() {
        return ResponseEntity.ok(aiChatModelDao.selectList(Wrappers.lambdaQuery(AiClientModel.class)
                .orderByDesc(AiClientModel::getCreateTime)));
    }

    /**
     * 查询有效模型
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiModelResponseDTO>> getValidModelList() {
        return ResponseEntity.ok(aiChatModelDao.selectList(
                        Wrappers.lambdaQuery(AiClientModel.class)
                                .eq(AiClientModel::getStatus, 1)
                                .orderByDesc(AiClientModel::getCreateTime))
                .stream()
                .map(this::convertToResponseDTO)
                .toList());
    }

    /**
     * 分页查询模型
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiClientModel>> queryModelList(@RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(aiChatModelDao.selectList(
                Wrappers.lambdaQuery(AiClientModel.class)
                        .orderByDesc(AiClientModel::getCreateTime)
                        .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询模型详情
     */
    @GetMapping("/{modelId}")
    public ResponseEntity<AiClientModel> queryModelById(@PathVariable Long modelId) {
        return ResponseEntity.ok(aiChatModelDao.selectById(modelId));
    }

    /**
     * 新增模型
     */
    @PostMapping
    public ResponseEntity<String> createModel(@RequestBody @Validated(Groups.Create.class) AiModelRequestDTO requestDTO) {
        log.info("新增chatModel request:{}", requestDTO);

        AiClientModel aiClientModel = convertToModel(requestDTO);
        if (Objects.isNull(requestDTO.getModelVersion())) {
            List<String> modelList = openAiPort.modelList(requestDTO.getBaseUrl(), requestDTO.getCompletionsPath(), requestDTO.getApiKey());
            if (modelList.isEmpty()) return ResponseEntity.badRequest().build();
            aiChatModelDao.insertBatch(aiClientModel, modelList);
        } else
            aiChatModelDao.insert(aiClientModel);

        return ResponseEntity.ok("新增model成功");
    }


    /**
     * 更新模型
     */
    @PutMapping
    public ResponseEntity<String> updateModel(@RequestBody @Validated(Groups.Update.class) AiModelRequestDTO requestDTO) {
        Long modelId = requestDTO.getId();
        try {
            log.info("开始更新model id:{}", modelId);
            AiClientModel aiClientModel = convertToModel(requestDTO);

            aiChatModelDao.updateById(aiClientModel);
            List<AiClient> aiClients = aiClientDao.queryAiClientsByModelId(modelId);

            customBeanRegistrar.clearBean(Constants.BeanName.MODEL + modelId);
            aiAgentPreheatService.preheat(aiClients.stream().map(AiClient::getId).toArray(Long[]::new));

            return ResponseEntity.ok("更新model成功");
        } catch (Exception e) {
            log.error("更新model失败 modelId:{}", modelId, e);
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    /**
     * 变更模型状态
     */
    @PutMapping("/status")
    public ResponseEntity<Boolean> changeModelStatus(@RequestParam @NotNull Long modelId) {
        log.info("变更模型状态开始 modelId:{}", modelId);
        Long clientCount = aiClientDao.selectCount(Wrappers.lambdaQuery(AiClient.class)
                .eq(AiClient::getModelId, modelId));

        if (clientCount > 0) return ResponseEntity.ok(false);
        AiClientModel aiClientModel = aiChatModelDao.selectById(modelId);
        aiClientModel.setStatus(aiClientModel.getStatus().equals(1) ? 0 : 1);
        aiChatModelDao.updateById(aiClientModel);
        return ResponseEntity.ok(true);
    }

    /**
     * 删除模型
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteModel(@RequestParam @NotNull Long modelId) {
        log.info("删除模型开始 modelId:{}", modelId);
        int delete = aiChatModelDao.delete(Wrappers.<AiClientModel>lambdaQuery()
                .eq(AiClientModel::getId, modelId)
                .eq(AiClientModel::getStatus, 0));
        if (delete > 0) {
            customBeanRegistrar.clearBean(Constants.BeanName.MODEL + modelId);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    private AiModelResponseDTO convertToResponseDTO(AiClientModel clientModel) {
        if (clientModel == null) {
            return null;
        }
        AiModelResponseDTO aiModelResponseDTO = new AiModelResponseDTO();
        aiModelResponseDTO.setId(clientModel.getId());
        aiModelResponseDTO.setModelVersion(clientModel.getModelVersion());
        aiModelResponseDTO.setModelName(clientModel.getModelName());
        return aiModelResponseDTO;
    }

    private AiClientModel convertToModel(AiModelRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        AiClientModel aiClientModel = new AiClientModel();
        aiClientModel.setId(requestDTO.getId());
        aiClientModel.setModelName(requestDTO.getModelName());
        aiClientModel.setBaseUrl(requestDTO.getBaseUrl());
        aiClientModel.setApiKey(requestDTO.getApiKey());
        aiClientModel.setCompletionsPath(requestDTO.getCompletionsPath());
        aiClientModel.setModelType(requestDTO.getModelType());
        aiClientModel.setModelVersion(requestDTO.getModelVersion());
        aiClientModel.setTimeout(requestDTO.getTimeout());
        return aiClientModel;
    }
}
