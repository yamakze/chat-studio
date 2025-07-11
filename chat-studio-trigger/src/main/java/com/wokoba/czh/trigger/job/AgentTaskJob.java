package com.wokoba.czh.trigger.job;

import com.wokoba.czh.domain.agent.model.entity.AiChatRequestEntity;
import com.wokoba.czh.domain.agent.model.entity.AiTaskScheduleEntity;
import com.wokoba.czh.domain.agent.model.valobj.ChatRetryAction;
import com.wokoba.czh.domain.agent.service.IAiChatService;
import com.wokoba.czh.domain.agent.service.task.AiAgentTaskService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 智能体任务调度作业
 * 定时获取有效的任务调度配置，并动态创建新的任务
 */
@Slf4j
@Component
public class AgentTaskJob implements DisposableBean {

    @Autowired
    private AiAgentTaskService aiAgentTaskService;

    @Resource
    private IAiChatService aiChatService;

    private TaskScheduler taskScheduler;

    /**
     * 任务ID与任务执行器的映射，用于记录已添加的任务
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化任务调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("agent-task-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        this.taskScheduler = scheduler;
        this.refreshTasks();
    }

    /**
     * 检查并更新任务调度配置
     */
    @Scheduled(fixedRate = 60000)
    public void refreshTasks() {
        try {
            // 获取所有有效的任务调度配置
            List<AiTaskScheduleEntity> taskSchedules = aiAgentTaskService.queryAllValidTaskSchedule();

            // 记录当前配置中的任务ID
            Map<Long, Boolean> currentTaskIds = new ConcurrentHashMap<>();

            // 处理每个任务调度配置
            for (AiTaskScheduleEntity task : taskSchedules) {
                Long taskId = task.getId();
                currentTaskIds.put(taskId, true);

                // 如果任务已经存在，则跳过
                if (scheduledTasks.containsKey(taskId)) {
                    continue;
                }

                // 创建并调度新任务
                scheduleTask(task);
            }

            // 移除已不存在的任务
            scheduledTasks.keySet().removeIf(taskId -> {
                if (!currentTaskIds.containsKey(taskId)) {
                    ScheduledFuture<?> future = scheduledTasks.remove(taskId);
                    if (future != null) {
                        future.cancel(true);
                        log.info("已移除任务，ID: {}", taskId);
                    }
                    return true;
                }
                return false;
            });

            log.info("任务调度配置刷新完成，当前活跃任务数: {}", scheduledTasks.size());
        } catch (Exception e) {
            log.error("刷新任务调度配置时异常", e);
        }
    }

    /**
     * 清理数据库中的无效任务
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void cleanInvalidTasks() {
        log.info("开始清理无效的智能体任务");
        try {
            // 获取所有无效的任务ID
            List<Long> invalidTaskIds = aiAgentTaskService.queryAllInvalidTaskScheduleIds();

            if (invalidTaskIds == null || invalidTaskIds.isEmpty()) {
                log.info("没有发现无效的任务需要清理");
                return;
            }

            log.info("发现{}个无效任务需要清理", invalidTaskIds.size());

            // 从调度器中移除这些任务
            for (Long taskId : invalidTaskIds) {
                ScheduledFuture<?> future = scheduledTasks.remove(taskId);
                if (future != null) {
                    future.cancel(true);
                    log.info("已移除无效任务，ID: {}", taskId);
                }
            }

            log.info("无效任务清理完成，当前活跃任务数: {}", scheduledTasks.size());
        } catch (Exception e) {
            log.error("清理无效任务时发生错误", e);
        }
    }

    private void scheduleTask(AiTaskScheduleEntity task) {
        try {
            log.info("开始调度任务，ID: {}, 描述: {}, Cron表达式: {}", task.getId(), task.getDescription(), task.getCronExpression());

            // 创建任务执行器
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeTask(task),
                    new CronTrigger(task.getCronExpression())
            );

            // 记录任务
            scheduledTasks.put(task.getId(), future);

            log.info("任务调度成功，ID: {}", task.getId());
        } catch (Exception e) {
            log.error("调度任务时发生错误，ID: {}", task.getId(), e);
        }
    }


    private void executeTask(AiTaskScheduleEntity task) {
        try {
            log.info("开始执行任务，ID: {}, 描述: {}", task.getId(), task.getDescription());
            // 执行任务
            ChatResponse chatResponse = aiChatService.aiChat(new AiChatRequestEntity()
                    .setClientId(task.getAgentId())
                    .setUserMessage(task.getTaskParam()));

            aiAgentTaskService.recordTaskCompleted(task.getId(),
                    task.getTaskParam(),
                    chatResponse.getResult().getOutput().getText(),
                    chatResponse.getMetadata().getUsage().getTotalTokens());
            log.info("任务执行完成，ID: {},content:{}", task.getId(), chatResponse.getResult().getOutput().getText());
        } catch (Exception e) {
            aiAgentTaskService.recordTaskFailure(task.getId(), task.getTaskParam());
            log.error("执行任务时发生错误，ID: {}", task.getId(), e);
        }
    }

    @Override
    public void destroy() {
        // 关闭时取消所有任务
        scheduledTasks.forEach((id, future) -> {
            if (future != null) {
                future.cancel(true);
                log.info("已取消任务，ID: {}", id);
            }
        });
        scheduledTasks.clear();
        log.info("所有智能体任务已取消");
    }
}