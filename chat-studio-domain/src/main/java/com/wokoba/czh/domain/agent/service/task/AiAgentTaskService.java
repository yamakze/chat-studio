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
        return repository.findAllValidSchedulesWithActiveClient();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return repository.queryAllInvalidTaskScheduleIds();
    }

    public void recordTaskCompleted(Long taskId, String request, String response, Integer totalTokens) {
        repository.insertTaskExecutionRecord(taskId,request,response,totalTokens,"completed");
    }

    public void recordTaskFailure(Long taskId, String request) {
        repository.insertTaskExecutionRecord(taskId,request,"无",0,"failure");
    }
}