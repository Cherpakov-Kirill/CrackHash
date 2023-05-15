package ru.nsu.fit.crackhash.manager.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.manager.model.CrackTask;
import ru.nsu.fit.crackhash.manager.model.TaskStatus;
import ru.nsu.fit.crackhash.manager.rebbitmq.RabbitMqProducer;
import ru.nsu.fit.crackhash.manager.repository.CrackTaskRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CrackService {

    @Value("${crackHash.alphabet}")
    private String alphabetString;

    @Value("${crackHash.partCount}")
    private int partCount;

    @Value("${crackHash.timeout}")
    private long timeout;

    private final CrackTaskRepository repository;

    private final RabbitMqProducer rabbitMqProducer;

    private final CrackHashManagerRequest.Alphabet alphabet;

    public CrackService(RabbitMqProducer rabbitMqProducer, CrackTaskRepository repository) {
        this.rabbitMqProducer = rabbitMqProducer;
        alphabet = new CrackHashManagerRequest.Alphabet();
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        alphabet.getSymbols().addAll(Arrays.stream(alphabetString.split("")).toList());
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
            rabbitMqProducer.sendMessage(request);
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        return requestId;
    }

    public CrackTask getTask(String requestId) {
        Optional<CrackTask> optionalCrackTask = repository.findById(requestId);
        if (optionalCrackTask.isPresent()) {
            CrackTask task = optionalCrackTask.get();
//            task.checkOnTimeout(timeout);
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
            if (!task.getReadyParts().contains(result.getPartNumber())) {
                task.addResults(result.getAnswers().getWords());
                repository.save(task);
                log.info("Saved crack task result for {} part of {} task", result.getPartNumber(), result.getRequestId());
            } else {
                log.info("Crack task result for {} part of {} task was successfully saved", result.getPartNumber(), result.getRequestId());
            }
        }
    }
}
