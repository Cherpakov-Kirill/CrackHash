package ru.nsu.fit.crackhash.manager.controler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.manager.api.mapper.CrackTaskMapper;
import ru.nsu.fit.crackhash.manager.service.CrackService;

@RestController
@RequestMapping("/internal/api/manager/")
@Slf4j
public class WorkerController {
    private final CrackService crackService;
    private final CrackTaskMapper mapper;

    public WorkerController(CrackService crackService, CrackTaskMapper mapper) {
        this.crackService = crackService;
        this.mapper = mapper;
    }

    @PatchMapping(path = "/hash/crack/request", produces = MediaType.APPLICATION_JSON_VALUE)
    public void setTask(@RequestBody CrackHashWorkerResponse request) {
        crackService.handleResult(request);
    }
}
