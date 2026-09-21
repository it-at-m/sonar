package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Only the records carry the {@code is} prefix on a boolean. An entity cannot. MapStruct reads an
 * entity property off its Lombok getter and strips the {@code is} there
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungEntityMapper {

    @Mapping(target = "isZustellungsbevollmaechtigterGenutzt", source = "zustellungsbevollmaechtigterGenutzt")
    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "zustellungsbevollmaechtigterGenutzt", source = "isZustellungsbevollmaechtigterGenutzt")
    AbrechnungEntity toEntity(Abrechnung abrechnung);

    @Mapping(target = ".", source = "adressdaten")
    AbrechnungNutzungsobjekt toNutzungsobjekt(AbrechnungNutzungsobjektEntity abrechnungNutzungsobjektEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adressdaten", source = "abrechnungNutzungsobjekt")
    AbrechnungNutzungsobjektEntity toEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @Mapping(target = "isHaelfte", source = "haelfte")
    AbrechnungPosition toPosition(AbrechnungPositionEntity abrechnungPositionEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "haelfte", source = "isHaelfte")
    AbrechnungPositionEntity toEntity(AbrechnungPosition abrechnungPosition);

}
