package com.wokoba.czh.trigger.http;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.api.dto.AiTaskExecutionRecordResponseDTO;
import com.wokoba.czh.api.dto.AiTaskScheduleRequestDTO;
import com.wokoba.czh.api.group.Groups;
import com.wokoba.czh.infrastructure.dao.AiAgentTaskScheduleDao;
import com.wokoba.czh.infrastructure.dao.AiTaskExecutionRecordDao;
import com.wokoba.czh.infrastructure.dao.po.AiAgentTaskSchedule;
import com.wokoba.czh.infrastructure.dao.po.AiTaskExecutionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
@Slf4j
public class AiTaskController {

    @Autowired
    private AiAgentTaskScheduleDao aiAgentTaskScheduleDao;
    @Autowired
    private AiTaskExecutionRecordDao aiTaskExecutionRecordDao;

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<String> createTask(@RequestBody @Validated(Groups.Create.class) AiTaskScheduleRequestDTO requestDTO) {
        try {
            AiAgentTaskSchedule taskSchedule = convertToTaskSchedule(requestDTO);
            aiAgentTaskScheduleDao.insert(taskSchedule);
            return ResponseEntity.ok("创建任务成功");
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新任务
     */
    @PutMapping
    public ResponseEntity<String> updateTask(@RequestBody @Validated(Groups.Update.class) AiTaskScheduleRequestDTO requestDTO) {
        try {
            AiAgentTaskSchedule taskSchedule = convertToTaskSchedule(requestDTO);
            aiAgentTaskScheduleDao.updateById(taskSchedule);
            return ResponseEntity.ok("更新任务成功");
        } catch (Exception e) {
            log.error("更新任务失败 taskId:{}", requestDTO.getId(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除任务（仅可删除未启用的任务）
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Boolean> deleteTask(@PathVariable Long taskId) {
        try {
            int delete = aiAgentTaskScheduleDao.delete(Wrappers.lambdaQuery(AiAgentTaskSchedule.class)
                    .eq(AiAgentTaskSchedule::getId, taskId)
                    .eq(AiAgentTaskSchedule::getStatus, 0));
            return ResponseEntity.ok(delete > 0);
        } catch (Exception e) {
            log.error("删除任务失败 taskId:{}", taskId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<AiAgentTaskSchedule> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(aiAgentTaskScheduleDao.selectById(taskId));
    }

    /**
     * 查询全部任务
     */
    @GetMapping
    public ResponseEntity<List<AiAgentTaskSchedule>> getAllTasks() {
        return ResponseEntity.ok(aiAgentTaskScheduleDao.selectList(Wrappers.lambdaQuery(AiAgentTaskSchedule.class).orderByDesc(AiAgentTaskSchedule::getCreateTime)));
    }

    /**
     * 查询有效任务
     */
    @GetMapping("/valid")
    public ResponseEntity<List<AiAgentTaskSchedule>> getValidTasks() {
        return ResponseEntity.ok(aiAgentTaskScheduleDao.selectList(Wrappers.lambdaQuery(AiAgentTaskSchedule.class)
                .eq(AiAgentTaskSchedule::getStatus, 1)
                .orderByDesc(AiAgentTaskSchedule::getCreateTime)));
    }

    /**
     * 查询任务执行日志
     */
    @GetMapping("/log/{taskId}")
    public ResponseEntity<List<AiTaskExecutionRecordResponseDTO>> getTaskExecutionRecords(@PathVariable Long taskId) {
        List<AiTaskExecutionRecord> aiTaskExecutionRecords = aiTaskExecutionRecordDao.selectList(Wrappers.lambdaQuery(AiTaskExecutionRecord.class)
                .eq(AiTaskExecutionRecord::getTaskId, taskId)
                .orderByDesc(AiTaskExecutionRecord::getExecuteTime)
                .last("limit 1000"));

        return ResponseEntity.ok(aiTaskExecutionRecords.stream().map(this::convertToTaskRecordResponseDTO).toList());
    }

    private AiAgentTaskSchedule convertToTaskSchedule(AiTaskScheduleRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        AiAgentTaskSchedule aiAgentTaskSchedule = new AiAgentTaskSchedule();
        aiAgentTaskSchedule.setId(requestDTO.getId());
        aiAgentTaskSchedule.setAgentId(requestDTO.getAgentId());
        aiAgentTaskSchedule.setTaskName(requestDTO.getTaskName());
        aiAgentTaskSchedule.setDescription(requestDTO.getDescription());
        aiAgentTaskSchedule.setCronExpression(requestDTO.getCronExpression());
        aiAgentTaskSchedule.setTaskParam(requestDTO.getTaskParam());
        aiAgentTaskSchedule.setStatus(requestDTO.getStatus());
        return aiAgentTaskSchedule;
    }

    private AiTaskExecutionRecordResponseDTO convertToTaskRecordResponseDTO(AiTaskExecutionRecord taskExecutionRecord) {
        if (taskExecutionRecord == null) {
            return null;
        }
        AiTaskExecutionRecordResponseDTO aiTaskExecutionRecordResponseDTO = new AiTaskExecutionRecordResponseDTO();
        aiTaskExecutionRecordResponseDTO.setRequest(taskExecutionRecord.getRequest());
        aiTaskExecutionRecordResponseDTO.setResponse(taskExecutionRecord.getResponse());
        aiTaskExecutionRecordResponseDTO.setTotalTokens(taskExecutionRecord.getTotalTokens());
        aiTaskExecutionRecordResponseDTO.setStatus(taskExecutionRecord.getStatus());
        aiTaskExecutionRecordResponseDTO.setExecuteTime(taskExecutionRecord.getExecuteTime());
        return aiTaskExecutionRecordResponseDTO;
    }
}
