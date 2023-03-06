package ru.nsu.fit.crackhash.manager.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Getter
@ToString
@Builder(toBuilder = true)
public class CreateCrackTaskRequestDTO {

    @JsonProperty(value = "hash", required = true)
    String hash;

    @JsonProperty(value = "maxLength", required = true)
    Integer maxLength;

}
