package ru.nsu.fit.crackhash.manager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.fit.crackhash.manager.model.CrackTask;

public interface CrackTaskRepository extends MongoRepository<CrackTask, String> {
}
