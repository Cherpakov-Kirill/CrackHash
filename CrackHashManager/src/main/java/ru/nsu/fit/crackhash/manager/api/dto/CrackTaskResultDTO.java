package ru.nsu.fit.crackhash.manager.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Getter
@ToString
@Builder(toBuilder = true)
public class CrackTaskResultDTO {

    @JsonProperty(value = "status", required = true)
    String status;

    @JsonProperty(value = "data", required = true)
    List<String> data;

}
