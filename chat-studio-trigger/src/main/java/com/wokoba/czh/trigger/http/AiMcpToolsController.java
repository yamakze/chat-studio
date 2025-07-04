package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.api.dto.AiToolRequestDTO;
import com.wokoba.czh.api.group.Groups;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.armory.AiAgentPreheatService;
import com.wokoba.czh.infrastructure.dao.AiClientToolConfigDao;
import com.wokoba.czh.infrastructure.dao.AiClientToolMcpDao;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolConfig;
import com.wokoba.czh.infrastructure.dao.po.AiClientToolMcp;
import com.wokoba.czh.types.common.Constants;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@Slf4j
public class AiMcpToolsController {
    @Autowired
    private AiClientToolMcpDao aiClientToolMcpDao;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AiClientToolConfigDao aiClientToolConfigDao;
    @Autowired
    private AiAgentPreheatService aiAgentPreheatService;
    @Autowired
    private CustomBeanRegistrar customBeanRegistrar;

    /**
     * 查询全部工具
     */
    @GetMapping
    public ResponseEntity<List<AiClientToolMcp>> getAllTools() {
        return ResponseEntity.ok(aiClientToolMcpDao.selectList(Wrappers.lambdaQuery(AiClientToolMcp.class).orderByDesc(AiClientToolMcp::getCreateTime)));
    }

    /**
     * 查询有效工具
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiClientToolMcp>> getValidTools() {
        return ResponseEntity.ok(aiClientToolMcpDao.selectList(Wrappers.lambdaQuery(AiClientToolMcp.class)
                .eq(AiClientToolMcp::getStatus, 1)
                .orderByDesc(AiClientToolMcp::getCreateTime)));
    }

    /**
     * 分页查询工具
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiClientToolMcp>> getToolsPage(@RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(aiClientToolMcpDao.selectList(Wrappers.lambdaQuery(AiClientToolMcp.class)
                .orderByDesc(AiClientToolMcp::getCreateTime)
                .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询工具详情
     */
    @GetMapping("/{toolId}")
    public ResponseEntity<AiClientToolMcp> getToolsById(@PathVariable Long toolId) {
        return ResponseEntity.ok(aiClientToolMcpDao.selectById(toolId));
    }

    /**
     * 创建工具
     */
    @PostMapping
    public ResponseEntity<String> createTool(@RequestBody @Validated(Groups.Create.class) AiToolRequestDTO requestDTO) {
        try {
            log.info("创建mcp工具开始 toolMcp:{}", requestDTO);
            objectMapper.readTree(requestDTO.getTransportConfig());
            AiClientToolMcp aiClientToolMcp = convertToAiTool(requestDTO);
            aiClientToolMcpDao.insert(aiClientToolMcp);
            return ResponseEntity.ok("Tool created");
        } catch (JsonProcessingException e) {
            log.error("创建mcp工具失败 toolMcp:{}", requestDTO, e);
            return ResponseEntity.badRequest().body("工具创建失败");
        }
    }

    /**
     * 更新工具
     */
    @PutMapping
    public ResponseEntity<String> updateTool(@RequestBody @Validated(Groups.Update.class) AiToolRequestDTO requestDTO) {
        try {
            log.info("更新mcp工具开始 toolMcp:{}", requestDTO);
            objectMapper.readTree(requestDTO.getTransportConfig());
            AiClientToolMcp aiClientToolMcp = convertToAiTool(requestDTO);
            aiClientToolMcpDao.updateById(aiClientToolMcp);

            customBeanRegistrar.clearBean(Constants.BeanName.MCP_TOOL + requestDTO.getId());
            List<Long> clientIds = aiClientToolConfigDao.queryClientIdsByToolId(requestDTO.getId());
            for (Long clientId : clientIds) {
                aiAgentPreheatService.preheat(clientId);
            }
            return ResponseEntity.ok("更新tool成功");
        } catch (Exception e) {
            log.error("更新mcp工具失败 toolMcp:{}", requestDTO, e);
            return ResponseEntity.badRequest().body("更新tool失败");
        }
    }

    /**
     * 变更工具状态
     */
    @PutMapping("/status")
    public ResponseEntity<Boolean> changeMcpStatus(@RequestParam @NotNull Long toolId) {
        log.info("变更MCP工具状态开始 toolId:{}", toolId);
        Long clientCount = aiClientToolConfigDao.selectCount(Wrappers.lambdaQuery(AiClientToolConfig.class)
                .eq(AiClientToolConfig::getToolId, toolId));

        if (clientCount > 0) return ResponseEntity.badRequest().build();
        AiClientToolMcp aiClientToolMcp = aiClientToolMcpDao.selectById(toolId);
        aiClientToolMcp.setStatus(aiClientToolMcp.getStatus().equals(1) ? 0 : 1);
        aiClientToolMcpDao.updateById(aiClientToolMcp);
        return ResponseEntity.ok(true);
    }

    /**
     * 删除工具
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteTool(@RequestParam @NotNull Long toolId) {
        log.info("删除MCP工具开始 toolId:{}", toolId);
        int delete = aiClientToolMcpDao.delete(Wrappers.<AiClientToolMcp>lambdaQuery()
                .eq(AiClientToolMcp::getId, toolId)
                .eq(AiClientToolMcp::getStatus, 0));
        if (delete > 0) {
            customBeanRegistrar.clearBean(Constants.BeanName.MCP_TOOL + toolId);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.badRequest().build();
    }

    private AiClientToolMcp convertToAiTool(AiToolRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        AiClientToolMcp aiClientToolMcp = new AiClientToolMcp();
        aiClientToolMcp.setId(requestDTO.getId());
        aiClientToolMcp.setMcpName(requestDTO.getMcpName());
        aiClientToolMcp.setTransportType(requestDTO.getTransportType());
        aiClientToolMcp.setTransportConfig(requestDTO.getTransportConfig());
        aiClientToolMcp.setRequestTimeout(requestDTO.getRequestTimeout());
        aiClientToolMcp.setBasic(requestDTO.getBasic());
        return aiClientToolMcp;
    }

}
