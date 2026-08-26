package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.projekt.domain.ProjektAdresse;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProjektEntityMapper {

    Projekt toProjekt(ProjektEntity projektEntity);

    @Mapping(target = "id", ignore = true)
    ProjektEntity toEntity(Projekt projekt);

    @Mapping(target = "id", ignore = true)
    ProjektAdresseEntity toEntity(ProjektAdresse projektAdresse);

}
