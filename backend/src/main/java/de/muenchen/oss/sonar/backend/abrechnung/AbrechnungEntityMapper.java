package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungEntityMapper {

    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity);

    @Mapping(target = "id", ignore = true)
    AbrechnungEntity toEntity(Abrechnung abrechnung);

    @Mapping(target = "id", ignore = true)
    AbrechnungNutzungsobjektEntity toEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @Mapping(target = "id", ignore = true)
    AbrechnungPositionEntity toEntity(AbrechnungPosition abrechnungPosition);

}
