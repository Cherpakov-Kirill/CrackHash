package ru.nsu.fit.crackhash.worker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.fit.crackhash.worker.service.WorkerService;

@RestController
@RequestMapping("/internal/api/worker")
@Tag(name = "user")
@Slf4j
public class ManagerController {

    private final WorkerService workerService;

    public ManagerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(path = "/hash/crack/task", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set crack hash task")
    @ApiResponse(responseCode = "200", description = "Success")
    public void setTask(@RequestBody CrackHashManagerRequest request) {
        workerService.initTask(request);
    }
}
