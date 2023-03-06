package ru.nsu.fit.crackhash.worker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.fit.crackhash.worker.service.WorkerService;

@RestController
@RequestMapping("/internal/api/worker")
@Slf4j
public class ManagerController {

    private final WorkerService workerService;

    public ManagerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(path = "/hash/crack/task", produces = MediaType.APPLICATION_JSON_VALUE)
    public void setTask(@RequestBody CrackHashManagerRequest request) {
        workerService.initTask(request);
    }
}
