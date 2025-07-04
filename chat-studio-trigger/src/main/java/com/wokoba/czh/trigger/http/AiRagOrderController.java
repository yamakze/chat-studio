package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.domain.agent.model.valobj.GitRepoEntity;
import com.wokoba.czh.domain.agent.service.IAiRagService;
import com.wokoba.czh.infrastructure.dao.AiRagOrderDao;
import com.wokoba.czh.infrastructure.dao.po.AiRagOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rag")
@Slf4j
public class AiRagOrderController {
    @Autowired
    private AiRagOrderDao aiRagOrderDao;

    @Autowired
    private IAiRagService aiRagService;


    /**
     * 查询全部知识库
     */
    @GetMapping
    public ResponseEntity<List<AiRagOrder>> getAllRagOrders() {
        return ResponseEntity.ok(aiRagOrderDao.selectList(Wrappers.<AiRagOrder>lambdaQuery().orderByDesc(AiRagOrder::getCreateTime)));
    }

    /**
     * 查询有效知识库
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiRagOrder>> getValidRagOrders() {
        return ResponseEntity.ok(aiRagOrderDao.selectList(Wrappers.<AiRagOrder>lambdaQuery()
                .eq(AiRagOrder::getStatus, 1)
                .orderByDesc(AiRagOrder::getCreateTime)));
    }

    /**
     * 分页查询知识库
     */
    @GetMapping("/page")
    public ResponseEntity<List<AiRagOrder>> getAllRagOrders(@RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseEntity.ok(aiRagOrderDao.selectList(
                Wrappers.<AiRagOrder>lambdaQuery()
                        .orderByDesc(AiRagOrder::getCreateTime)
                        .last("limit " + pageSize + " offset " + page)));
    }

    /**
     * 查询知识库详情
     */
    @GetMapping("/{ragId}")
    public ResponseEntity<AiRagOrder> getRagOrder(@PathVariable Long ragId) {
        return ResponseEntity.ok(aiRagOrderDao.selectById(ragId));
    }

    /**
     * 创建知识库（建议用DTO封装参数）
     */
    @PostMapping
    public ResponseEntity<String> createRagOrder(@RequestParam @NotBlank String ragName, @RequestParam @NotBlank String knowledgeTag, @RequestParam List<MultipartFile> files) {
        log.info("创建知识库 ragName: {}", ragName);
        aiRagService.storeRagOrder(ragName, knowledgeTag, files.stream().map(MultipartFile::getResource).collect(Collectors.toList()));
        return ResponseEntity.ok("知识库创建成功");
    }

    /**
     * 解析Git仓库
     */
    @PostMapping("/git")
    public ResponseEntity<String> analyzeGitRepository(@RequestParam @NotBlank String repoUrl, @RequestParam @NotBlank String userName, @RequestParam @NotBlank String token) {
        try {
            log.info("克隆路径 ：{}", repoUrl);

            aiRagService.storeGitRepo(GitRepoEntity.builder()
                    .repoUrl(repoUrl)
                    .token(token)
                    .userName(userName)
                    .build());

            log.info("遍历解析路径，上传完成:{}", repoUrl);

            return ResponseEntity.ok("解析仓库完成");
        } catch (Exception e) {
            log.error("解析代码仓库失败 url:{}", repoUrl, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/{ragId}")
    public ResponseEntity<Boolean> deleteRagOrder(@PathVariable Long ragId) {
        log.info("删除知识库开始 ragId:{}", ragId);

        int deleted = aiRagService.deleteRagOrder(ragId);
        return ResponseEntity.ok(deleted > 0);
    }


}
