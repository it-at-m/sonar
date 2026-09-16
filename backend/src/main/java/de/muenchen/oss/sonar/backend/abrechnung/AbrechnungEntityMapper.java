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

    @Mapping(target = "widerspruchVorhanden", source = "widerspruchVorhanden")
    @Mapping(target = "neuereVersionVorhanden", source = "neuereVersionVorhanden")
    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity, boolean widerspruchVorhanden, boolean neuereVersionVorhanden);

    /**
     * The place in the chain of versions is left to the service. It follows from the Vorgänger and
     * never from the data that was entered.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionsnummer", ignore = true)
    @Mapping(target = "vorgaengerAbrechnungId", ignore = true)
    AbrechnungEntity toEntity(Abrechnung abrechnung);

    @Mapping(target = ".", source = "adressdaten")
    AbrechnungNutzungsobjekt toNutzungsobjekt(AbrechnungNutzungsobjektEntity abrechnungNutzungsobjektEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adressdaten", source = "abrechnungNutzungsobjekt")
    AbrechnungNutzungsobjektEntity toEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @Mapping(target = "id", ignore = true)
    AbrechnungPositionEntity toEntity(AbrechnungPosition abrechnungPosition);

}
