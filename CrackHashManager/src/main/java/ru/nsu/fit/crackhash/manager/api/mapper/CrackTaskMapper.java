package ru.nsu.fit.crackhash.manager.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.nsu.fit.crackhash.manager.api.dto.CrackTaskResultDTO;
import ru.nsu.fit.crackhash.manager.model.CrackTask;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CrackTaskMapper {
    CrackTaskResultDTO mapEntityToDto(CrackTask entity);
}
