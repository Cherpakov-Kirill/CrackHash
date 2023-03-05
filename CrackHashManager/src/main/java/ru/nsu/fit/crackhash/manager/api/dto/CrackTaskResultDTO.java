package ru.nsu.fit.crackhash.manager.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Schema(description = "Crack hash task")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@Getter
@ToString
@Builder(toBuilder = true)
public class CrackTaskResultDTO {

    @JsonProperty(value = "status", required = true)
    @NotNull
    String status;

    @JsonProperty(value = "data", required = true)
    List<String> data;

}
