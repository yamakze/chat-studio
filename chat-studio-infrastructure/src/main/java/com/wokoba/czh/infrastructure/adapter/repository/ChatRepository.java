package com.wokoba.czh.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.*;
import com.wokoba.czh.domain.agent.model.valobj.*;
import com.wokoba.czh.infrastructure.dao.*;
import com.wokoba.czh.infrastructure.dao.po.*;
import com.wokoba.czh.infrastructure.dao.po.AiClientSystemPrompt;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
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
    public List<AiClientMateriel> queryAiClientByClientIds(List<Long> clientIdList) {
        if (null == clientIdList || clientIdList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiClient> clientList = aiClientDao.selectBatchIds(clientIdList);
        //查询提示词
        Map<Long, String> aiClientSystemPrompts = aiClientSystemPromptDao.selectList(Wrappers.<AiClientSystemPrompt>lambdaQuery()
                        .in(AiClientSystemPrompt::getId, clientList.stream().map(AiClient::getSystemPromptId).collect(Collectors.toSet()))
                        .orderByDesc(AiClientSystemPrompt::getCreateTime)
                        .eq(AiClientSystemPrompt::getStatus, 1))
                .stream()
                .collect(Collectors.toMap(
                        AiClientSystemPrompt::getId,  // key: id
                        AiClientSystemPrompt::getPromptContent,        // value: AiClientSystemPromptContent
                        (existing, replacement) -> existing  // 如果有重复key，保留第一个
                ));
        ;
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
            AiClientMateriel clientVO = AiClientMateriel.builder()
                    .clientId(clientId)
                    .options(objectMapper.readValue(client.getOptions(), AiClientOptionsVO.class))
                    .systemPromptContent(aiClientSystemPrompts.get(client.getSystemPromptId()))
                    .modelId(client.getModelId())
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
    public void updateClientConfig(Long clientId, Long systemPromptId, Long modelId, List<Long> mcpIdList, List<Long> advisorIdList, String optionsJsonStr) {
        transactionTemplate.executeWithoutResult(status -> {
            if (advisorIdList != null) {
                aiClientAdvisorConfigDao.deleteBatchByClientId(clientId);
                if (!advisorIdList.isEmpty())
                    aiClientAdvisorConfigDao.insertBatch(clientId, advisorIdList);
            }
            if (mcpIdList != null) {
                aiClientToolConfigDao.deleteBatchByClientId(clientId);
                if (!mcpIdList.isEmpty())
                    aiClientToolConfigDao.insertBatch(clientId, mcpIdList);
            }
            aiClientDao.update(Wrappers.lambdaUpdate(AiClient.class)
                    .eq(AiClient::getId, clientId)
                    .set(AiClient::getModelId, modelId)
                    .set(StringUtils.isNotBlank(optionsJsonStr), AiClient::getOptions, optionsJsonStr)
                    .set(AiClient::getSystemPromptId, systemPromptId));
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
                .orderByAsc(AiClientSystemPrompt::getCreateTime)
                .last("limit 1")).getId();

        Long defaultModelId = aiChatModelDao.selectOne(Wrappers
                .lambdaQuery(AiClientModel.class)
                .select(AiClientModel::getId)
                .orderByAsc(AiClientModel::getCreateTime)
                .last("limit 1")).getId();

        return AiClientMateriel.builder()
                .advisorIdList(advisorIdList)
                .mcpIdList(mcpIdList)
                .modelId(defaultModelId)
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
            //数据库配置
            //{
            //    "zhipu-web-search-sse": {
            //      "baseUri": "xx"
            //    }
            //}
            Map<String, AiClientToolMcpEntity.TransportConfigSse.SseConfig> sseConfigMap = objectMapper.readValue(transportConfig, new TypeReference<Map<String, AiClientToolMcpEntity.TransportConfigSse.SseConfig>>() {
            });
            AiClientToolMcpEntity.TransportConfigSse.SseConfig sseConfig = sseConfigMap.values().stream().findFirst().orElse(null);
            sseConfigMap = Map.of(vo.getMcpName(), sseConfig);
            vo.setTransportConfigSse(new AiClientToolMcpEntity.TransportConfigSse(sseConfigMap));
        } else if ("stdio".equals(transportType)) {
            // 解析STDIO配置
//            Map<String, AiClientToolMcpEntity.TransportConfigStdio.Stdio> mcpToStdioMap =
//                    JSON.parseObject(transportConfig,
//                            new com.alibaba.fastjson.TypeReference<>() {
//                            });
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

}
