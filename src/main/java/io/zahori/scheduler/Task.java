package io.zahori.scheduler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Task.class);

    @NotNull
    private UUID uuid;

    @NotBlank //(message = "cronExpression is mandatory")
    private String cronExpression;

    @JsonIgnore
    ScheduledFuture<?> scheduledFuture;

    public Task(UUID uuid, String cronExpression) {
        this.uuid = uuid;
        this.cronExpression = cronExpression;
    }

    @Override
    public String toString() {
        return "Task [uuid: " + uuid + " --> cron: " + cronExpression + "]";
    }

    @Override
    public void run() {
        // Code to be run when the schedule is executed
        TaskSchedulerService.getInstance().run(this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

}
