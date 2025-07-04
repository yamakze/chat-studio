package com.wokoba.czh.infrastructure.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wokoba.czh.domain.agent.model.entity.AiTaskScheduleEntity;
import com.wokoba.czh.infrastructure.dao.po.AiAgentTaskSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 智能体任务调度配置数据访问接口
 */
@Mapper
public interface AiAgentTaskScheduleDao extends BaseMapper<AiAgentTaskSchedule> {

    default List<AiTaskScheduleEntity> getTaskScheduleByStatus(Integer status) {
        return selectList(Wrappers.<AiAgentTaskSchedule>lambdaQuery()
                .eq(AiAgentTaskSchedule::getStatus, status))
                .stream().map(this::buildAiTaskScheduleVO)
                .toList();

    }

    default AiTaskScheduleEntity buildAiTaskScheduleVO(AiAgentTaskSchedule aiAgentTaskSchedule) {
        if (aiAgentTaskSchedule == null) {
            return null;
        }
        AiTaskScheduleEntity aiTaskScheduleEntity = new AiTaskScheduleEntity();
        aiTaskScheduleEntity.setId(aiAgentTaskSchedule.getId());
        aiTaskScheduleEntity.setAgentId(aiAgentTaskSchedule.getAgentId());
        aiTaskScheduleEntity.setDescription(aiAgentTaskSchedule.getDescription());
        aiTaskScheduleEntity.setCronExpression(aiAgentTaskSchedule.getCronExpression());
        aiTaskScheduleEntity.setTaskParam(aiAgentTaskSchedule.getTaskParam());
        return aiTaskScheduleEntity;
    }

}