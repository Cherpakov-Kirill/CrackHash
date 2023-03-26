package ru.nsu.fit.crackhash.manager.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.manager.model.CrackTask;
import ru.nsu.fit.crackhash.manager.model.TaskStatus;
import ru.nsu.fit.crackhash.manager.repository.CrackTaskRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CrackService {
    private static final String apiWorkerUrl = "/internal/api/worker/hash/crack/task";

    @Value("${crackHash.worker.ip}")
    private String workerIp;

    @Value("${crackHash.worker.port}")
    private String workerPort;

    @Value("${crackHash.alphabet}")
    private String alphabetString;

    @Value("${crackHash.partCount}")
    private int partCount;

    @Value("${crackHash.timeout}")
    private long timeout;

    private String workerUrl;

    @Autowired
    private CrackTaskRepository repository;

    private final RestTemplate restTemplate;

    private final CrackHashManagerRequest.Alphabet alphabet;

    public CrackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        alphabet = new CrackHashManagerRequest.Alphabet();
    }

    @PostConstruct
    private void init() {
        workerUrl = "http://" + workerIp + ":" + workerPort;

        alphabet.getSymbols().addAll(Arrays.stream(alphabetString.split("")).toList());
    }

    public void sendTask(CrackHashManagerRequest request) {
        log.info("Set {} part of {} task request was sent", request.getPartNumber(), request.getRequestId());
        restTemplate.postForObject(workerUrl + apiWorkerUrl, request, String.class);
    }

    public String setTask(String hash, Integer maxLength) {
        String requestId = UUID.randomUUID().toString();
        CrackTask task = new CrackTask(requestId, hash, maxLength, partCount);
        repository.save(task);

        CrackHashManagerRequest request = new CrackHashManagerRequest();
        request.setRequestId(requestId);
        request.setPartCount(partCount);
        request.setHash(hash);
        request.setMaxLength(maxLength);
        request.setAlphabet(alphabet);

        for (int i = 1; i <= partCount; i++) {
            request.setPartNumber(i);
            sendTask(request);
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        return requestId;
    }

    public CrackTask getTask(String requestId) {
        Optional<CrackTask> optionalCrackTask = repository.findById(requestId);
        if (optionalCrackTask.isPresent()) {
            CrackTask task = optionalCrackTask.get();
            task.checkOnTimeout(timeout);
            return task;
        }
        log.info("CrackTask with requestId = '{}' not found", requestId);
        return null;
    }

    public void handleResult(CrackHashWorkerResponse result) {
        log.info("Handled crack task result for {} part of {} task", result.getPartNumber(), result.getRequestId());
        Optional<CrackTask> optionalCrackTask = repository.findById(result.getRequestId());
        if (optionalCrackTask.isPresent()) {
            CrackTask task = optionalCrackTask.get();
            task.addResults(result.getAnswers().getWords());
            repository.save(task);
            log.info("Saved crack task result for {} part of {} task", result.getPartNumber(), result.getRequestId());
        }
    }
}
