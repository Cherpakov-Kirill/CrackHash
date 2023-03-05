package ru.nsu.fit.crackhash.manager.controler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.nsu.fit.crackhash.manager.api.dto.CrackTaskResultDTO;
import ru.nsu.fit.crackhash.manager.api.mapper.CrackTaskMapper;
import ru.nsu.fit.crackhash.manager.api.dto.CreateCrackTaskRequestDTO;
import ru.nsu.fit.crackhash.manager.api.dto.CreateCrackTaskResponseDTO;
import ru.nsu.fit.crackhash.manager.model.CrackTask;
import ru.nsu.fit.crackhash.manager.service.CrackService;

@RestController
@RequestMapping("/api/hash")
@Tag(name = "user")
@Slf4j
public class UserController {
    private final CrackService crackService;
    private final CrackTaskMapper mapper;

    public UserController(CrackService crackService, CrackTaskMapper mapper) {
        this.crackService = crackService;
        this.mapper = mapper;
    }

    @PostMapping(path = "/crack", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set crack hash task")
    @ApiResponse(responseCode = "200", description = "Success")
    public @ResponseBody CreateCrackTaskResponseDTO setTask(@RequestBody CreateCrackTaskRequestDTO request) {
        String requestId = crackService.setTask(request.getHash(), request.getMaxLength());
        CreateCrackTaskResponseDTO createCrackTaskResponseDTO = new CreateCrackTaskResponseDTO(requestId);
        log.info("POST crack task response = {}", createCrackTaskResponseDTO);
        return createCrackTaskResponseDTO;
    }

    @GetMapping(path = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get crack hash task status")
    @ApiResponse(responseCode = "200", description = "Success")
    public @ResponseBody CrackTaskResultDTO getTaskStatus(@RequestParam("requestId") String requestId) {
        CrackTask task = crackService.getTask(requestId);
        CrackTaskResultDTO crackTaskResultDTO = mapper.mapEntityToDto(task);
        log.info("GET crack task result response = {}",  crackTaskResultDTO);
        return crackTaskResultDTO;
    }
}
