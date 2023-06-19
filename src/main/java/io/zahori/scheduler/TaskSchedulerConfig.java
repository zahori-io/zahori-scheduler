package io.zahori.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class TaskSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        // Pool size: número máximo de hilos en ejecución al mismo tiempo. 
        // Si el pool está lleno y otro nuevo hilo requiere ejecutarse, este se quedará esperando hasta que alguno de los hilos en ejecución termine
        // y este pase a ejecutarse. 
        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("Task");
        return threadPoolTaskScheduler;
    }
}
