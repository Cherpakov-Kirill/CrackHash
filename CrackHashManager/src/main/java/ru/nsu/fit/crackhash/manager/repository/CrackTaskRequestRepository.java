package ru.nsu.fit.crackhash.manager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.fit.crackhash.manager.model.CrackTaskRequest;

public interface CrackTaskRequestRepository extends MongoRepository<CrackTaskRequest, String> {
}
