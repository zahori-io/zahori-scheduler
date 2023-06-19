package io.zahori.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class TaskSchedulerApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSchedulerApplicationListener.class);

    @Autowired
    private ZahoriServerService zahoriServer;

    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("Application started event");

        LOG.info("Initalizing scheduler...");

        // Más ejemplos: https://www.baeldung.com/spring-task-scheduler
        // Generador de expresiones cron: https://www.freeformatter.com/cron-expression-generator-quartz.html
        //        Task task = new Task(1, "0/5 * * * * *");
        //        taskSchedulerSevice.add(task);
        // Cada minuto de 9 a 18 de lunes a viernes (At second :01, every minute, every hour between 09am and 18pm, on every Monday, Tuesday, Wednesday, Thursday and Friday, every month)
        //        Task task2 = new Task(2, "1 * 9-18 ? * MON,TUE,WED,THU,FRI");
        //        taskSchedulerSevice.add(task2);
        //        Thread.sleep(31000);
        //        delete(task);
        LOG.info("============== SCHEDULER STARTED ==============");
        zahoriServer.waitZahoriServerHealthcheck();
        zahoriServer.loadTasks();
        taskSchedulerService.printScheduledTasks();

        //        taskSchedulerSevice.validCronExpression("1 * 9-18 ? * MON,TUE,WED,THU,FRI");
        //        taskSchedulerSevice.validCronExpression("1 * 9-18 * * MON,TUE,WED,THU,FRI");
        //        taskSchedulerSevice.validCronExpression("1 * 9-18 ? * MON,TUE,WED,THU,FRI *");
    }

}
