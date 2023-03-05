package ru.nsu.fit.crackhash.worker.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class WorkerService {
    private static final String apiManagerUrl = "/internal/api/manager/hash/crack/request";

    @Value("${crackHashManager.ip}")
    private String managerIp;

    @Value("${crackHashManager.port}")
    private String managerPort;

    private String managerUrl;
    private final RestTemplate restTemplate;

    public WorkerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void init() {
        managerUrl = "http://" + managerIp + ":" + managerPort;
    }

    public void sendResult(CrackHashWorkerResponse request) {
        log.info("Result of {} part of {} task was sent", request.getPartNumber(), request.getRequestId());
        restTemplate.patchForObject(managerUrl + apiManagerUrl, request, String.class);
    }

    public void initTask(CrackHashManagerRequest request) {
        log.info("Handled task : hash = {}, length = {}, part = {}/{}", request.getHash(), request.getMaxLength(), request.getPartNumber(), request.getPartCount());

        Thread thread = new Thread(() -> WorkerService.this.runTask(request));
        thread.start();
    }

    private void runTask(CrackHashManagerRequest request) {
        ICombinatoricsVector<String> initialVector = CombinatoricsFactory.createVector(request.getAlphabet().getSymbols());
        List<Generator<String>> generators = initGenerators(initialVector, request.getMaxLength());

        List<String> answerStrings = new LinkedList<>();
        int length = 0;
        for(Generator<String> generator : generators) {
            length++;
            Iterator<ICombinatoricsVector<String>> iterator = generator.iterator();

            long count = generator.getNumberOfGeneratedObjects();
            long countPerPart = count / request.getPartCount();
            long startIteratorPos = countPerPart * (request.getPartNumber() - 1);
            long countOfPermsForCompute = request.getPartCount() == request.getPartNumber() ? count - startIteratorPos : countPerPart;
            for (int i = 0; i < startIteratorPos; i++) {
                iterator.next();
            }
            log.info("ResultId '{}' : Started the calculations {}/{} part for length = {} : [{}; {}]",
                    request.getRequestId(),
                    request.getPartNumber(),
                    request.getPartCount(),
                    length,
                    startIteratorPos,
                    startIteratorPos + countOfPermsForCompute - 1);

            String requestedHash = request.getHash();
            for (int i = 0; i < countOfPermsForCompute; i++) {
                String vector = String.join("", iterator.next().getVector());
                String hash = DigestUtils.md5Hex(vector);
                if (hash.equals(requestedHash)) {
                    answerStrings.add(vector);
                }
            }
        }


        CrackHashWorkerResponse response = new CrackHashWorkerResponse();
        response.setRequestId(request.getRequestId());
        response.setPartNumber(request.getPartNumber());
        CrackHashWorkerResponse.Answers answers = new CrackHashWorkerResponse.Answers();
        answers.getWords().addAll(answerStrings);
        response.setAnswers(answers);
        sendResult(response);
    }

    private List<Generator<String>> initGenerators(ICombinatoricsVector<String> initialVector, int maxLength){
        List<Generator<String>> list = new LinkedList<>();
        for (int length = 1; length <= maxLength; length++){
            list.add(CombinatoricsFactory.createMultiCombinationGenerator(initialVector, length));
        }
        return list;
    }
}
