package com.wokoba.czh.domain.agent.service.task;

import com.wokoba.czh.domain.agent.adapter.repository.IChatRepository;
import com.wokoba.czh.domain.agent.model.entity.AiTaskScheduleEntity;
import com.wokoba.czh.domain.agent.service.IAiAgentTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAgentTaskService implements IAiAgentTaskService {

    @Resource
    private IChatRepository repository;

    @Override
    public List<AiTaskScheduleEntity> queryAllValidTaskSchedule() {
        return repository.queryAllValidTaskSchedule();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return repository.queryAllInvalidTaskScheduleIds();
    }

}