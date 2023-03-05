package ru.nsu.fit.crackhash.manager.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.manager.model.CrackTask;
import ru.nsu.fit.crackhash.manager.model.TaskStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, CrackTask> map;

    private final RestTemplate restTemplate;

    private final CrackHashManagerRequest.Alphabet alphabet;

    public CrackService(RestTemplate restTemplate) {
        map = new ConcurrentHashMap<>();
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
        CrackTask task = new CrackTask(hash, maxLength, partCount);
        map.put(requestId, task);

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
        CrackTask task = map.get(requestId);
        task.checkOnTimeout(timeout);
        return task;
    }

    public void handleResult(CrackHashWorkerResponse result) {
        log.info("Handled crack task result for {} part of {} task", result.getPartNumber(), result.getRequestId());
        CrackTask task = map.get(result.getRequestId());
        task.addResults(result.getAnswers().getWords());
    }
}
