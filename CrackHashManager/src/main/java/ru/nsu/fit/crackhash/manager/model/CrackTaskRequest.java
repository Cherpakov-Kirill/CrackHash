package ru.nsu.fit.crackhash.manager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

import java.util.UUID;

@Data
@Document(collection = "crackTaskRequestCollection")
public class CrackTaskRequest {
    @Id
    private String id;
    private CrackHashManagerRequest request;

    public CrackTaskRequest(CrackHashManagerRequest request) {
        this.id = String.valueOf(UUID.randomUUID());
        this.request = request;
    }
}
