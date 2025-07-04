package com.wokoba.czh.domain.agent.service;

import com.wokoba.czh.domain.agent.model.entity.AiTaskScheduleEntity;

import java.util.List;


public interface IAiAgentTaskService {

    List<AiTaskScheduleEntity> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

}
