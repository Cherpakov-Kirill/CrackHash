package ru.nsu.fit.crackhash.manager.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Create crack hash task response")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Getter
@ToString
@Builder(toBuilder = true)
public class CreateCrackTaskResponseDTO {

    @JsonProperty(value = "requestId", required = true)
    @NotNull
    String requestId;

}
