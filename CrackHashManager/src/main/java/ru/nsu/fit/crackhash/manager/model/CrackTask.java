package ru.nsu.fit.crackhash.manager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "crackTaskCollection")
public class CrackTask {
    @Id
    private String id;
    private String hash;
    private Integer maxLength;
    private int partCount;
    private int successParts;
    private TaskStatus status;
    private Instant startTimestamp;
    private List<String> data;
    private Set<Integer> readyParts;

    public CrackTask(String id, String hash, Integer maxLength, int partCount) {
        this.id = id;
        this.hash = hash;
        this.maxLength = maxLength;
        this.partCount = partCount;
        this.successParts = 0;
        this.status = TaskStatus.CREATED;
        this.data = new LinkedList<>();
        this.readyParts = new HashSet<>();
        this.startTimestamp = Instant.now();
    }

    public void addResults(List<String> words) {
        successParts++;
        if (successParts == partCount && status != TaskStatus.ERROR) {
            status = TaskStatus.READY;
        }
        data.addAll(words);
    }

    public void checkOnTimeout(long timeoutMillis) {
        if (successParts == partCount) return;
        if (Instant.now().toEpochMilli() - startTimestamp.toEpochMilli() > timeoutMillis) {
            status = TaskStatus.ERROR;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "CrackTask[id=%s, hash='%s', maxLength='%s', partCount='%s', status='%s']",
                id, hash, maxLength, partCount, status);
    }
}
