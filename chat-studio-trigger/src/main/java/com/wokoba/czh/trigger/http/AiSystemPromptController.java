package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.api.dto.AiPromptRequestDTO;
import com.wokoba.czh.api.group.Groups;
import com.wokoba.czh.domain.agent.service.armory.AiAgentPreheatService;
import com.wokoba.czh.infrastructure.dao.AiClientDao;
import com.wokoba.czh.infrastructure.dao.AiClientSystemPromptDao;
import com.wokoba.czh.infrastructure.dao.po.AiClient;
import com.wokoba.czh.infrastructure.dao.po.AiClientSystemPrompt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/prompt")
public class AiSystemPromptController {

    private final AiClientSystemPromptDao aiClientSystemPromptDao;
    private final AiAgentPreheatService aiAgentPreheatService;
    private final AiClientDao aiClientDao;

    public AiSystemPromptController(AiClientSystemPromptDao aiClientSystemPromptDao, AiAgentPreheatService aiAgentPreheatService, AiClientDao aiClientDao) {
        this.aiClientSystemPromptDao = aiClientSystemPromptDao;
        this.aiAgentPreheatService = aiAgentPreheatService;
        this.aiClientDao = aiClientDao;
    }

    /**
     * 查询全部系统提示词
     */
    @GetMapping
    public ResponseEntity<List<AiClientSystemPrompt>> getAllSystemPrompts() {
        return ResponseEntity.ok(aiClientSystemPromptDao.selectList(Wrappers.<AiClientSystemPrompt>lambdaQuery()
                .orderByDesc(AiClientSystemPrompt::getCreateTime)));
    }

    /**
     * 分页查询系统提示词
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiClientSystemPrompt>> getSystemPromptsPage(@RequestParam(defaultValue = "1") Integer page,
                                                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(aiClientSystemPromptDao.selectList(Wrappers.<AiClientSystemPrompt>lambdaQuery()
                .orderByDesc(AiClientSystemPrompt::getCreateTime)
                .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询有效系统提示词
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiClientSystemPrompt>> getValidSystemPrompts() {
        return ResponseEntity.ok(aiClientSystemPromptDao.selectList(Wrappers.<AiClientSystemPrompt>lambdaQuery()
                .orderByDesc(AiClientSystemPrompt::getCreateTime)
                .eq(AiClientSystemPrompt::getStatus, 1)));
    }

    /**
     * 新增系统提示词
     */
    @PostMapping
    public ResponseEntity<String> saveSystemPrompt(@RequestBody @Validated(Groups.Create.class) AiPromptRequestDTO requestDTO) {
        log.info("新增系统提示词 promptName: {}, description: {}", requestDTO.getPromptName(), requestDTO.getDescription());
        AiClientSystemPrompt systemPrompt = convertToSystemPrompt(requestDTO);
        aiClientSystemPromptDao.insert(systemPrompt);
        return ResponseEntity.ok("新增提示词成功");
    }

    /**
     * 删除系统提示词
     */
    @DeleteMapping
    public ResponseEntity<String> deleteSystemPrompt(@RequestParam @NonNull Long promptId) {
        try {
            log.info("开始删除系统提示词 id: {}", promptId);
            aiClientSystemPromptDao.deleteById(promptId);
            List<AiClient> aiClients = aiClientDao.queryAiClientSByPromptId(promptId);
            aiAgentPreheatService.preheat(aiClients.stream().map(AiClient::getId).toArray(Long[]::new));
            return ResponseEntity.ok("删除系统提示词成功");
        } catch (Exception e) {
            log.error("删除系统提示词失败", e);
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    /**
     * 更新系统提示词
     */
    @PutMapping
    public ResponseEntity<String> updateSystemPrompt(@Validated(Groups.Update.class) @RequestBody AiPromptRequestDTO requestDTO) {
        try {
            log.info("更新系统提示词开始 id: {}", requestDTO.getId());
            AiClientSystemPrompt prompt=convertToSystemPrompt(requestDTO);
            aiClientSystemPromptDao.updateById(prompt);

            List<AiClient> aiClients = aiClientDao.queryAiClientSByPromptId(requestDTO.getId());
            aiAgentPreheatService.preheat(aiClients.stream().map(AiClient::getId).toArray(Long[]::new));
            return ResponseEntity.ok("更新系统提示词成功");
        } catch (Exception e) {
            log.error("更新系统提示词失败", e);
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    private AiClientSystemPrompt convertToSystemPrompt(AiPromptRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        AiClientSystemPrompt aiClientSystemPrompt = new AiClientSystemPrompt();
        aiClientSystemPrompt.setId(requestDTO.getId());
        aiClientSystemPrompt.setPromptName(requestDTO.getPromptName());
        aiClientSystemPrompt.setPromptContent(requestDTO.getPromptContent());
        aiClientSystemPrompt.setDescription(requestDTO.getDescription());
        aiClientSystemPrompt.setStatus(requestDTO.getStatus());
        return aiClientSystemPrompt;
    }


}
