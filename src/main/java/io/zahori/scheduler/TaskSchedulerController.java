package io.zahori.scheduler;

import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TaskSchedulerController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSchedulerController.class);

    private final TaskSchedulerService service;

    public TaskSchedulerController(TaskSchedulerService taskSchedulerService) {
        super();
        this.service = taskSchedulerService;
    }

    @GetMapping("healthcheck")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("Scheduler is up", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Object> getTasks() {
        Iterable<UUID> tasks = service.getList();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<Object> getTask(@PathVariable UUID uuid) {
        Task task = service.get(uuid);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addTask(@Valid @RequestBody Task task) {
        service.add(task);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Object> updateTask(@Valid @RequestBody Task task) {
        service.update(task);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "/{taskUuid}")
    public ResponseEntity<Object> deleteTask(@PathVariable UUID taskUuid) {
        service.delete(taskUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
