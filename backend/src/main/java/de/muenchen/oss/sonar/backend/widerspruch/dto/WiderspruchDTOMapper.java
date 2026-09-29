package de.muenchen.oss.sonar.backend.widerspruch.dto;

import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WiderspruchDTOMapper {

    WiderspruchResponseDTO toDTO(Widerspruch widerspruch);

    @Mapping(target = "id", ignore = true)
    Widerspruch toWiderspruch(WiderspruchRequestDTO widerspruchRequestDTO);

}
