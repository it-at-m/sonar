package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungEntityMapper {

    @Mapping(target = "neuereVersionVorhanden", source = "neuereVersionVorhanden")
    @Mapping(target = "nutzungsobjekte", source = "nutzungsobjekte")
    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity, boolean neuereVersionVorhanden,
            List<AbrechnungNutzungsobjekt> nutzungsobjekte);

    @Mapping(target = ".", source = "nutzungsobjektEntity.adressdaten")
    AbrechnungNutzungsobjekt toNutzungsobjekt(NutzungsobjektEntity nutzungsobjektEntity,
            List<AbrechnungPosition> positionen);

    AbrechnungPosition toPosition(AbrechnungPositionEntity abrechnungPositionEntity);

    /**
     * The service assembles the Nutzungsobjekte and the Positionen. It also sets the place in the
     * chain of versions. That place follows from the Vorgänger and never from the data that was
     * entered.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "widerspruch", ignore = true)
    @Mapping(target = "versionsnummer", ignore = true)
    @Mapping(target = "vorgaengerAbrechnungId", ignore = true)
    @Mapping(target = "nutzungsobjekte", ignore = true)
    @Mapping(target = "positionen", ignore = true)
    AbrechnungEntity toEntity(Abrechnung abrechnung);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adressdaten", source = "abrechnungNutzungsobjekt")
    NutzungsobjektEntity toEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nutzungsobjekt", ignore = true)
    AbrechnungPositionEntity toEntity(AbrechnungPosition abrechnungPosition);

}
