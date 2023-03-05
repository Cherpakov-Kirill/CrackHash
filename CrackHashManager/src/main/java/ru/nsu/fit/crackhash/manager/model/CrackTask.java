package ru.nsu.fit.crackhash.manager.model;

import lombok.Data;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Data
public class CrackTask {
    private String hash;
    private Integer maxLength;
    private int partCount;
    private int successParts;
    private TaskStatus status;
    private Instant startTimestamp;
    private List<String> data;

    public CrackTask(String hash, Integer maxLength, int partCount){
        this.hash = hash;
        this.maxLength = maxLength;
        this.partCount = partCount;
        this.successParts = 0;
        this.status = TaskStatus.CREATED;
        this.data = new LinkedList<>();
        this.startTimestamp = Instant.now();
    }

    public void addResults(List<String> words){
        successParts++;
        if (successParts == partCount) {
            status = TaskStatus.READY;
        }
        data.addAll(words);
    }

    public void checkOnTimeout(long timeoutMillis) {
        if (successParts == partCount) return;
        if(Instant.now().toEpochMilli() - startTimestamp.toEpochMilli() > timeoutMillis){
            status = TaskStatus.ERROR;
        }
    }
}
