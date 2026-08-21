package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import org.mapstruct.Mapper;

@Mapper
public interface ProjektMapper {

    ProjektResponseDTO toDTO(ProjektView projektView);

    CreateProjektCommand toCreateCommand(ProjektRequestDTO projektRequestDTO);
}
