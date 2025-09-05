package com.wokoba.czh.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.*;
import com.wokoba.czh.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.wokoba.czh.domain.agent.model.valobj.AiClientOptionsVO;
import com.wokoba.czh.domain.agent.model.valobj.AiClientVO;
import com.wokoba.czh.infrastructure.adapter.port.OpenAiPort;
import com.wokoba.czh.infrastructure.dao.*;
import com.wokoba.czh.infrastructure.dao.po.*;
import com.wokoba.czh.types.enums.ResponseCode;
import com.wokoba.czh.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ChatRepository implements IChatRepository {

    @Resource
    private AiChatModelDao aiChatModelDao;
    @Resource
    private AiClientToolConfigDao aiClientToolConfigDao;

    @Resource
    private AiClientToolMcpDao aiClientToolMcpDao;

    @Resource
    private AiClientAdvisorDao aiClientAdvisorDao;

    @Resource
    private AiClientSystemPromptDao aiClientSystemPromptDao;
    @Resource
    private AiClientAdvisorConfigDao aiClientAdvisorConfigDao;
    @Autowired
    private AiAgentFlowConfigDao aiAgentFlowConfigDao;
    @Resource
    private AiRagOrderDao aiRagOrderDao;
    @Resource
    private AiClientDao aiClientDao;

    @Resource
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AiAgentTaskScheduleDao aiAgentTaskScheduleDao;
    @Autowired
    private AiTaskExecutionRecordDao aiTaskExecutionRecordDao;
    @Autowired
    private AiClientModelConfigDao aiClientModelConfigDao;
    @Autowired
    private OpenAiPort openAiPort;

    @Override
    public List<AiClientModelEntity> queryAiClientModelVOListByClientIds(List<Long> clientIdList) {
        List<Long> modelIds = aiClientDao.queryModelIdsByClientIds(clientIdList);
        return aiChatModelDao.queryAiClientModelEntityByIds(modelIds);
    }

    @Override
    public List<AiClientToolMcpEntity> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList) {
        // 根据客户端ID列表获取工具配置信息
        List<Long> mcpIds = aiClientToolConfigDao.queryMcpIdsByClientIds(clientIdList);
        List<AiClientToolMcp> aiClientToolMcps = aiClientToolMcpDao.queryAiClientToolMcpByMcpIds(mcpIds);
        return aiClientToolMcps.stream().map(this::conversion2AiClientToolMcpVO).toList();
    }

    @Override
    public List<AiClientAdvisorEntity> queryAdvisorConfigByClientIds(List<Long> clientIdList) {
        // 根据客户端ID列表获取顾问配置信息
        List<Long> advisorIds = aiClientAdvisorConfigDao.queryAdvisorIdsByClientIds(clientIdList);
        return aiClientAdvisorDao.queryAdvisorEntityByIds(advisorIds);
    }


    @SneakyThrows
    @Override
    public List<AiClientMateriel> queryClientMaterielByClientIds(List<Long> clientIdList) {
        if (null == clientIdList || clientIdList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiClient> clientList = aiClientDao.selectBatchIds(clientIdList);
        //查询模型配置
        Map<Long, AiClientModelConfig> modelConfigMap = aiClientModelConfigDao.selectList(Wrappers.lambdaQuery(AiClientModelConfig.class)
                        .in(AiClientModelConfig::getId, clientList.stream().map(AiClient::getModelConfigId).toList()))
                .stream()
                .collect(Collectors.toMap(AiClientModelConfig::getId, Function.identity()));
        //查询提示词
        Map<Long, String> aiClientSystemPromptMap = aiClientSystemPromptDao.selectList(Wrappers.<AiClientSystemPrompt>lambdaQuery()
                        .in(AiClientSystemPrompt::getId, clientList.stream().map(AiClient::getSystemPromptId).collect(Collectors.toSet()))
                        .orderByDesc(AiClientSystemPrompt::getCreateTime)
                        .eq(AiClientSystemPrompt::getStatus, 1))
                .stream()
                .collect(Collectors.toMap(
                        AiClientSystemPrompt::getId,
                        AiClientSystemPrompt::getPromptContent
                ));

        // 查询MCP工具配置，暂时只有 mcp，无 function call
        List<AiClientToolConfig> clientToolConfigs = aiClientToolConfigDao.queryToolConfigByClientIds(clientIdList);
        Map<Long, List<AiClientToolConfig>> mcpMap = clientToolConfigs.stream()
                .filter(config -> "mcp".equals(config.getToolType()))
                .collect(Collectors.groupingBy(AiClientToolConfig::getClientId));

        // 查询顾问配置
        List<AiClientAdvisorConfig> advisorConfigs = aiClientAdvisorConfigDao.queryClientAdvisorConfigByClientIds(clientIdList);
        Map<Long, List<AiClientAdvisorConfig>> advisorConfigMap = advisorConfigs.stream()
                .collect(Collectors.groupingBy(AiClientAdvisorConfig::getClientId));

        // 构建AiClientVO列表
        List<AiClientMateriel> result = new ArrayList<>();
        for (AiClient client : clientList) {
            Long clientId = client.getId();
            //设置客户端模型配置
            AiClientModelConfig aiClientModelConfig = modelConfigMap.getOrDefault(
                    client.getModelConfigId(),
                    modelConfigMap.values()
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new AppException(ResponseCode.MISS_CLIENT_MATERIALS))
            );

            //设置客户端基础配置
            AiClientMateriel clientVO = AiClientMateriel.builder()
                    .clientId(clientId)
                    .options(objectMapper.readValue(aiClientModelConfig.getOptions(), AiClientOptionsVO.class))
                    .systemPromptContent(aiClientSystemPromptMap.getOrDefault(client.getSystemPromptId(), "你是一个ai智能体"))
                    .modelId(aiClientModelConfig.getModelId())
                    .modelVersion(aiClientModelConfig.getModelVersion())
                    .systemPromptId(client.getSystemPromptId())
                    .build();
            // 设置MCP工具ID列表
            if (mcpMap.containsKey(clientId)) {
                List<Long> mcpBeanIdList = mcpMap.get(clientId).stream()
                        .map(AiClientToolConfig::getToolId)
                        .collect(Collectors.toList());
                clientVO.setMcpIdList(mcpBeanIdList);
            } else {
                clientVO.setMcpIdList(new ArrayList<>());
            }

            // 设置顾问ID列表
            if (advisorConfigMap.containsKey(clientId)) {
                List<Long> advisorBeanIdList = advisorConfigMap.get(clientId).stream()
                        .map(AiClientAdvisorConfig::getAdvisorId)
                        .collect(Collectors.toList());
                clientVO.setAdvisorIdList(advisorBeanIdList);
            } else {
                clientVO.setAdvisorIdList(new ArrayList<>());
            }

            result.add(clientVO);
        }

        return result;
    }

    @Override
    public List<Long> queryAiClientIds() {
        return aiClientDao.queryAllClientIds();
    }

    @Override
    public String queryRagKnowledgeTag(Long ragId) {
        AiRagOrder aiRagOrder = aiRagOrderDao.selectById(ragId);
        return aiRagOrder == null ? null : aiRagOrder.getKnowledgeTag();
    }

    @Override
    public void storeRagOrder(String name, String tag) {
        AiRagOrder aiRagOrder = new AiRagOrder();
        aiRagOrder.setRagName(name);
        aiRagOrder.setKnowledgeTag(tag);
        aiRagOrderDao.insert(aiRagOrder);
    }

    @Override
    public void updateClientConfig(AiClientMateriel materiel) {
        Long clientId = materiel.getClientId();
        List<Long> advisorIdList = materiel.getAdvisorIdList();
        List<Long> mcpIdList = materiel.getMcpIdList();
        transactionTemplate.executeWithoutResult(status -> {
            try {
                if (Objects.nonNull(advisorIdList)) {
                    aiClientAdvisorConfigDao.deleteBatchByClientId(clientId);
                    if (!advisorIdList.isEmpty())
                        aiClientAdvisorConfigDao.insertBatch(clientId, advisorIdList);
                }
                if (Objects.nonNull(mcpIdList)) {
                    aiClientToolConfigDao.deleteBatchByClientId(clientId);
                    if (!mcpIdList.isEmpty())
                        aiClientToolConfigDao.insertBatch(clientId, mcpIdList);
                }

                int modelConfigId = upsertModelConfig(materiel);

                aiClientDao.update(Wrappers.lambdaUpdate(AiClient.class)
                        .eq(AiClient::getId, clientId)
                        .set(AiClient::getSystemPromptId, materiel.getSystemPromptId())
                        .set(AiClient::getModelConfigId, modelConfigId));
            } catch (JsonProcessingException e) {
                log.error("modelConfig options Json 序列化失败，clientId={}", clientId, e);
                status.setRollbackOnly();
            } catch (Exception e) {
                log.error("更新客户端配置失败，clientId={}", clientId, e);
                status.setRollbackOnly();
                throw new AppException(ResponseCode.UN_ERROR);
            }
        });
    }

    @Override
    public int deleteRagOrder(Long ragId) {
        return aiRagOrderDao.deleteById(ragId);
    }

    @Override
    public AiClientMateriel queryClientBasicMaterials() {
        List<Long> advisorIdList = aiClientAdvisorDao.queryBasicAdvisorIds();
        List<Long> mcpIdList = aiClientToolMcpDao.queryBasicToolIds();
        Long defaultPromptId = aiClientSystemPromptDao.selectOne(Wrappers
                        .lambdaQuery(AiClientSystemPrompt.class)
                        .select(AiClientSystemPrompt::getId)
                        .orderByDesc(AiClientSystemPrompt::getUpdateTime)
                        .last("limit 1"))
                .getId();

        AiClientModel aiClientModel = aiChatModelDao.selectOne(Wrappers
                .lambdaQuery(AiClientModel.class)
                .orderByDesc(AiClientModel::getUpdateTime)
                .last("limit 1"));

        if (Objects.isNull(aiClientModel)) throw new AppException(ResponseCode.AI_MODEL_MISSING);
        List<String> modelVersionList = openAiPort.modelList(aiClientModel.getCompletionsUrl(), aiClientModel.getApiKey());
        if (modelVersionList.isEmpty()) throw new AppException(ResponseCode.MODEL_SUPPLIER_EXCEPTION);

        return AiClientMateriel.builder()
                .advisorIdList(advisorIdList)
                .mcpIdList(mcpIdList)
                .modelId(aiClientModel.getId())
                .modelVersion(modelVersionList.get(0))
                .systemPromptId(defaultPromptId)
                .build();
    }

    @Override
    public Long initAiClient() {
        AiClient client = new AiClient();
        client.setClientName("默认话题");
        client.setDescription("暂无描述");
        aiClientDao.insert(client);
        return client.getId();
    }

    @Override
    public List<AiTaskScheduleEntity> findAllValidSchedulesWithActiveClient() {
        List<AiTaskScheduleEntity> schedules = aiAgentTaskScheduleDao.getTaskScheduleByStatus(1);
        if (schedules.isEmpty()) return List.of();
        Set<Long> activeAgentIds = aiClientDao.selectBatchIds(
                schedules.stream().map(AiTaskScheduleEntity::getAgentId).toList()
        ).stream().map(AiClient::getId).collect(Collectors.toSet());

        return schedules.stream()
                .filter(schedule -> {
                    Long agentId = schedule.getAgentId();
                    boolean isActive = activeAgentIds.contains(agentId);
                    if (!isActive) {
                        log.warn("agent 配置缺失或无效。agentId={}", agentId);
                    }
                    return isActive;
                })
                .toList();
    }


    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return aiAgentTaskScheduleDao.getTaskScheduleByStatus(0)
                .stream().map(AiTaskScheduleEntity::getId)
                .toList();
    }

    @Override
    public void deleteClientById(Long clientId) {
        transactionTemplate.executeWithoutResult(status -> {
            aiClientDao.deleteById(clientId);
            aiClientAdvisorConfigDao.delete(Wrappers.lambdaQuery(AiClientAdvisorConfig.class).eq(AiClientAdvisorConfig::getClientId, clientId));
            aiClientToolConfigDao.delete(Wrappers.lambdaQuery(AiClientToolConfig.class).eq(AiClientToolConfig::getClientId, clientId));
            aiClientModelConfigDao.delete(Wrappers.lambdaQuery(AiClientModelConfig.class).eq(AiClientModelConfig::getClientId, clientId));
        });
    }

    @Override
    public void insertTaskExecutionRecord(Long taskId, String request, String response, Integer totalTokens, String status) {
        aiTaskExecutionRecordDao.insert(AiTaskExecutionRecord.builder()
                .taskId(taskId)
                .request(request)
                .response(response)
                .totalTokens(totalTokens)
                .status(status)
                .build());
    }

    @Override
    public Map<String, AiAgentClientFlowConfigVO> queryAiAgentClientFlowConfig(String aiAgentId) {
        if (aiAgentId == null || aiAgentId.trim().isEmpty()) {
            return Map.of();
        }
        // 根据智能体ID查询流程配置列表
        List<AiAgentFlowConfig> flowConfigs = aiAgentFlowConfigDao.queryByAgentId(aiAgentId);

        if (flowConfigs == null || flowConfigs.isEmpty()) {
            return Map.of();
        }
        // 针对相同的clientId，选择sequence较小的值作为最终结果
        return flowConfigs.stream()
                .map(flowConfig -> AiAgentClientFlowConfigVO.builder()
                        .clientId(flowConfig.getClientId())
                        .clientName(flowConfig.getClientName())
                        .clientType(flowConfig.getClientType())
                        .sequence(flowConfig.getSequence())
                        .stepPrompt(flowConfig.getStepPrompt())
                        .build())
                .collect(Collectors.toMap(
                        AiAgentClientFlowConfigVO::getClientType, Function.identity(),
                        BinaryOperator.minBy(Comparator.comparingInt(AiAgentClientFlowConfigVO::getSequence
                        ))));
    }

    @Override
    public AiClientVO queryClientBasicInfoById(Long clientId) {
        AiClient aiClient = aiClientDao.selectById(clientId);
        return new AiClientVO(clientId, aiClient.getClientName(), aiClient.getDescription(), aiClient.getStatus());
    }

    @SneakyThrows
    AiClientToolMcpEntity conversion2AiClientToolMcpVO(AiClientToolMcp aiClientToolMcp) {
        AiClientToolMcpEntity vo = new AiClientToolMcpEntity();
        vo.setId(aiClientToolMcp.getId());
        vo.setMcpName(aiClientToolMcp.getMcpName());
        vo.setTransportType(aiClientToolMcp.getTransportType());
        vo.setRequestTimeout(aiClientToolMcp.getRequestTimeout());

        // 根据传输类型解析JSON配置
        String transportType = aiClientToolMcp.getTransportType();
        String transportConfig = aiClientToolMcp.getTransportConfig();

        if ("sse".equals(transportType)) {
            // 解析SSE配置
            Map<String, AiClientToolMcpEntity.TransportConfigSse.SseConfig> sseConfigMap = objectMapper.readValue(transportConfig, new TypeReference<Map<String, AiClientToolMcpEntity.TransportConfigSse.SseConfig>>() {
            });
            AiClientToolMcpEntity.TransportConfigSse.SseConfig sseConfig = sseConfigMap.values().stream().findFirst().orElse(null);
            sseConfigMap = Map.of(vo.getMcpName(), sseConfig);
            vo.setTransportConfigSse(new AiClientToolMcpEntity.TransportConfigSse(sseConfigMap));
        } else if ("stdio".equals(transportType)) {
            // 解析STDIO配置
            Map<String, AiClientToolMcpEntity.TransportConfigStdio.Stdio> mcpToStdioMap = objectMapper.readValue(transportConfig, new TypeReference<Map<String, AiClientToolMcpEntity.TransportConfigStdio.Stdio>>() {
            });
            AiClientToolMcpEntity.TransportConfigStdio.Stdio targetStdio = mcpToStdioMap.values().stream().findFirst().orElse(null);

            // 构造新 map，使用 vo 的 mcpName 作为 key
            mcpToStdioMap = Map.of(vo.getMcpName(), targetStdio);

            AiClientToolMcpEntity.TransportConfigStdio stdioConfig = new AiClientToolMcpEntity.TransportConfigStdio();
            stdioConfig.setStdio(mcpToStdioMap);
            vo.setTransportConfigStdio(stdioConfig);
        }

        return vo;
    }

    private int upsertModelConfig(AiClientMateriel materiel) throws JsonProcessingException {
        return aiClientModelConfigDao.upsertByClientIdAndModelIdAndVersion(
                AiClientModelConfig.builder()
                        .clientId(materiel.getClientId())
                        .modelId(materiel.getModelId())
                        .modelVersion(materiel.getModelVersion())
                        .options(objectMapper.writeValueAsString(materiel.getOptions()))
                        .build()
        );
    }
}
