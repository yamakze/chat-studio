package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import com.wokoba.czh.domain.agent.service.IAiAgentPreheatService;
import com.wokoba.czh.infrastructure.dao.AiClientAdvisorConfigDao;
import com.wokoba.czh.infrastructure.dao.AiClientAdvisorDao;
import com.wokoba.czh.infrastructure.dao.po.AiClientAdvisor;
import com.wokoba.czh.infrastructure.dao.po.AiClientAdvisorConfig;
import com.wokoba.czh.types.common.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/advisor")
@Slf4j
public class AiAdvisorController {
    @Autowired
    private AiClientAdvisorDao aiClientAdvisorDao;
    @Autowired
    private AiClientAdvisorConfigDao aiClientAdvisorConfigDao;
    @Autowired
    private CustomBeanRegistrar customBeanRegistrar;
    @Autowired
    private IAiAgentPreheatService aiAgentPreheatService;
    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 分页查询顾问
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiClientAdvisor>> queryAdvisorPage(@RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(aiClientAdvisorDao.selectList(
                Wrappers.lambdaQuery(AiClientAdvisor.class)
                        .orderByDesc(AiClientAdvisor::getCreateTime)
                        .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询顾问详情
     */
    @GetMapping("/{advisorId}")
    public ResponseEntity<AiClientAdvisor> getAdvisor(@PathVariable Long advisorId) {
        return ResponseEntity.ok(aiClientAdvisorDao.selectById(advisorId));
    }

    /**
     * 查询全部顾问
     */
    @GetMapping
    public ResponseEntity<List<AiClientAdvisor>> queryAdvisorList() {
        return ResponseEntity.ok(aiClientAdvisorDao.selectList(Wrappers.lambdaQuery(AiClientAdvisor.class).orderByDesc(AiClientAdvisor::getCreateTime)));
    }

    /**
     * 查询有效顾问
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiClientAdvisor>> queryValidAdvisor() {
        return ResponseEntity.ok(aiClientAdvisorDao.selectList(Wrappers.lambdaQuery(AiClientAdvisor.class)
                .eq(AiClientAdvisor::getStatus, 1)
                .orderByDesc(AiClientAdvisor::getCreateTime)));
    }

    /**
     * 更新顾问
     */
    @PutMapping
    public ResponseEntity<String> updateAdvisor(@RequestParam @NotNull Long advisorId,
                                                @RequestParam @NotBlank String extParams,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) Boolean basic) {
        try {
            log.info("开始更新advisor advisorId:{}", advisorId);
            objectMapper.readTree(extParams); //校验 是否为合法 JSON 格式

            aiClientAdvisorDao.update(Wrappers.lambdaUpdate(AiClientAdvisor.class)
                    .eq(AiClientAdvisor::getId, advisorId)
                    .set(StringUtils.isNotBlank(name), AiClientAdvisor::getAdvisorName, name)
                    .set(AiClientAdvisor::getExtParam, extParams)
                    .set(AiClientAdvisor::getBasic, basic));

            List<Long> clientIds = aiClientAdvisorConfigDao.queryClientIdsByAdvisorId(advisorId);
            customBeanRegistrar.clearBean(Constants.BeanName.ADVISOR + advisorId);
            aiAgentPreheatService.preheat(clientIds.toArray(Long[]::new));

            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            log.error("更新advisor失败 advisorId:{}", advisorId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 变更顾问状态
     */
    @PutMapping("/status")
    public ResponseEntity<Boolean> changeAdvisorStatus(@RequestParam @NotNull Long advisorId) {
        log.info("变更advisor状态开始 advisorId:{}", advisorId);
        Long clientCount = aiClientAdvisorConfigDao.selectCount(Wrappers.lambdaQuery(AiClientAdvisorConfig.class)
                .eq(AiClientAdvisorConfig::getAdvisorId, advisorId));

        if (clientCount > 0) return ResponseEntity.badRequest().build();
        AiClientAdvisor aiClientAdvisor = aiClientAdvisorDao.selectById(advisorId);
        aiClientAdvisor.setStatus(aiClientAdvisor.getStatus().equals(1) ? 0 : 1);
        aiClientAdvisorDao.updateById(aiClientAdvisor);
        return ResponseEntity.ok(true);
    }

    /**
     * 删除顾问
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteAdvisor(@RequestParam @NotNull Long advisorId) {
        log.info("删除advisor开始 advisorId:{}", advisorId);
        int delete = aiClientAdvisorDao.delete(Wrappers.<AiClientAdvisor>lambdaQuery()
                .eq(AiClientAdvisor::getId, advisorId)
                .eq(AiClientAdvisor::getStatus, 0));
        if (delete > 0) {
            customBeanRegistrar.clearBean(Constants.BeanName.ADVISOR + advisorId);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.badRequest().build();
    }

}
