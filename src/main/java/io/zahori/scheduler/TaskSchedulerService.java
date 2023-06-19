package io.zahori.scheduler;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class TaskSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSchedulerService.class);

    private final Map<UUID, Task> scheduledTasks = new HashMap<>();

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ZahoriServerService zahoriServer;

    // Para poder usar este servicio desde un POJO (Task)
    private static TaskSchedulerService instance;

    // Para poder usar este servicio desde un POJO (Task)
    public static TaskSchedulerService getInstance() {
        return instance;
    }

    // Para poder usar este servicio desde un POJO (Task)
    @PostConstruct
    private void init() {
        instance = this;
    }

    public void printScheduledTasks() {
        LOG.info("");
        LOG.info("-------- Scheduled tasks --------");

        if (scheduledTasks.isEmpty()) {
            LOG.info("- No tasks");
        }

        for (Map.Entry<UUID, Task> e : scheduledTasks.entrySet()) {
            LOG.info("- " + e.getValue());
        }

        LOG.info("---------------------------------");
        LOG.info("");
    }

    public Set<UUID> getList() {
        printScheduledTasks();
        return scheduledTasks.keySet();
    }

    public Task get(UUID uuid) {
        if (taskIsNotPresent(uuid)) {
            throw new NotFoundException(uuid.toString());
        }
        return scheduledTasks.get(uuid);
    }

    public void add(Task task) {
        if (taskIsPresent(task.getUuid())) {
            throw new ConflictException(task.getUuid().toString());
        }

        schedule(task);
        LOG.info("[ + ] ADDED {}", task);

        printScheduledTasks();
    }

    public void update(Task task) {
        if (taskIsNotPresent(task.getUuid())) {
            throw new NotFoundException(task.getUuid().toString());
        }

        cancelTask(task.getUuid());
        schedule(task);
        LOG.info("[ + ] UPDATED {}", task);

        printScheduledTasks();
    }

    public void delete(UUID uuid) {
        if (taskIsNotPresent(uuid)) {
            throw new NotFoundException(uuid.toString());
        }

        cancelTask(uuid);
        LOG.info("[ - ] DELETED {}", uuid);

        printScheduledTasks();
    }

    public void run(Task task) {
        try {
            zahoriServer.runTask(task);
            LOG.info("[...] RUNNING {} --> OK", task);
        } catch (Exception e) {
            LOG.error("[...] ERROR RUNNING {} --> {}", task, e.getMessage());
        }

    }

    private boolean taskIsPresent(UUID taskId) {
        return scheduledTasks.containsKey(taskId);
    }

    private boolean taskIsNotPresent(UUID taskId) {
        return !taskIsPresent(taskId);
    }

    private void validateCronExpression(String cronExpression) {
        Runnable testTask = new Runnable() {
            @Override
            public void run() {
            }
        };

        ScheduledFuture<?> scheduledTask = schedule(testTask, cronExpression);
        cancelTask(scheduledTask);
    }

    private void schedule(Task task) {
        validateCronExpression(task.getCronExpression());
        ScheduledFuture<?> scheduledTask = schedule(task, task.getCronExpression());
        task.setScheduledFuture(scheduledTask);
        scheduledTasks.put(task.getUuid(), task);
    }

    private void cancelTask(UUID taskId) {
        cancelTask(scheduledTasks.get(taskId));
        scheduledTasks.remove(taskId);
    }

    private ScheduledFuture<?> schedule(Runnable runnable, String cronExpression) {
        try {
            return taskScheduler.schedule(runnable, new CronTrigger(cronExpression));
        } catch (Exception e) {
            throw new CronExpressionException(cronExpression);
        }
    }

    private void cancelTask(Task task) {
        cancelTask(task.getScheduledFuture());
    }

    private void cancelTask(ScheduledFuture scheduledTask) {
        scheduledTask.cancel(false);
    }

}
