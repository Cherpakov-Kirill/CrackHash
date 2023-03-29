package ru.nsu.fit.crackhash.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.fit.crackhash.worker.rebbitmq.RabbitMqProducer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class WorkerService {



    private final RabbitMqProducer rabbitMqProducer;
    private final ThreadPoolTaskExecutor taskExecutor;

    public WorkerService(RabbitMqProducer rabbitMqProducer, @Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.rabbitMqProducer = rabbitMqProducer;
        this.taskExecutor = taskExecutor;
    }

    public void initTask(CrackHashManagerRequest request) {
        log.info("Handled task : hash = {}, length = {}, part = {}/{}", request.getHash(), request.getMaxLength(), request.getPartNumber(), request.getPartCount());

        taskExecutor.execute(() -> WorkerService.this.runTask(request));
    }

    private void runTask(CrackHashManagerRequest request) {
        log.info("Executed task : hash = {}, length = {}, part = {}/{}", request.getHash(), request.getMaxLength(), request.getPartNumber(), request.getPartCount());
        ICombinatoricsVector<String> initialVector = CombinatoricsFactory.createVector(request.getAlphabet().getSymbols());
        List<Generator<String>> generators = initGenerators(initialVector, request.getMaxLength());

        List<String> answerStrings = new LinkedList<>();
        int length = 0;
        for (Generator<String> generator : generators) {
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
        rabbitMqProducer.sendMessage(response);
    }

    private List<Generator<String>> initGenerators(ICombinatoricsVector<String> initialVector, int maxLength) {
        List<Generator<String>> list = new LinkedList<>();
        for (int length = 1; length <= maxLength; length++) {
            list.add(CombinatoricsFactory.createPermutationWithRepetitionGenerator(initialVector, length));
        }
        return list;
    }
}
