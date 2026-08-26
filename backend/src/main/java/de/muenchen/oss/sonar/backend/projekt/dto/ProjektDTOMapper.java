package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.projekt.domain.ProjektAdresse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProjektDTOMapper {

    ProjektResponseDTO toDTO(Projekt projekt);

    @Mapping(target = "id", ignore = true)
    Projekt toProjekt(ProjektRequestDTO projektRequestDTO);

    @Mapping(target = "id", ignore = true)
    ProjektAdresse toProjektAdresse(ProjektAdresseRequestDTO projektAdresseRequestDTO);
}
