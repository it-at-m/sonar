package de.muenchen.oss.sonar.backend.projekt.model;

import de.muenchen.oss.sonar.backend.projekt.Projekt;
import de.muenchen.oss.sonar.backend.projekt.ProjektAdresse;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProjektModelMapper {

    ProjektView toView(Projekt projekt);

    @Mapping(target = "id", ignore = true)
    Projekt toEntity(CreateProjektCommand createProjektCommand);

    @Mapping(target = "id", ignore = true)
    ProjektAdresse toEntity(CreateProjektCommand.Adresse adresse);

}
