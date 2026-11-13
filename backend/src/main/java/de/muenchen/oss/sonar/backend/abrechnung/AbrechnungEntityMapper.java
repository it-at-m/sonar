package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungEntityMapper {

    @Mapping(target = "neuereVersionVorhanden", source = "neuereVersionVorhanden")
    @Mapping(target = "nutzungsobjekte", source = "nutzungsobjekte")
    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity, boolean neuereVersionVorhanden,
            List<AbrechnungNutzungsobjekt> nutzungsobjekte);

    @Mapping(target = ".", source = "nutzungsobjektEntity.adressdaten")
    AbrechnungNutzungsobjekt toNutzungsobjekt(NutzungsobjektEntity nutzungsobjektEntity, boolean changeable,
            List<AbrechnungPosition> positionen, List<AbrechnungMast> masten);

    AbrechnungPosition toPosition(AbrechnungPositionEntity abrechnungPositionEntity);

    AbrechnungMast toMast(AbrechnungMastEntity abrechnungMastEntity);

    /**
     * The service assembles the Nutzungsobjekte, the Positionen and the Masten. It also sets the place
     * in the chain of versions. That place follows from the Vorgänger and never from the data that was
     * entered. Whether the Abrechnung is a draft follows from the call to the service, too.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "widerspruch", ignore = true)
    @Mapping(target = "versionsnummer", ignore = true)
    @Mapping(target = "vorgaengerAbrechnungId", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "nutzungsobjekte", ignore = true)
    @Mapping(target = "positionen", ignore = true)
    @Mapping(target = "masten", ignore = true)
    AbrechnungEntity toEntity(Abrechnung abrechnung);

    @InheritConfiguration
    void updateEntity(Abrechnung abrechnung, @MappingTarget AbrechnungEntity abrechnungEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adressdaten", source = "abrechnungNutzungsobjekt")
    NutzungsobjektEntity toEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @InheritConfiguration
    @Mapping(target = "nutzungen", ignore = true)
    void updateEntity(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt, @MappingTarget NutzungsobjektEntity nutzungsobjektEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nutzungsobjekt", ignore = true)
    AbrechnungPositionEntity toEntity(AbrechnungPosition abrechnungPosition);

    @InheritConfiguration
    void updateEntity(AbrechnungPosition abrechnungPosition, @MappingTarget AbrechnungPositionEntity abrechnungPositionEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nutzungsobjekt", ignore = true)
    AbrechnungMastEntity toEntity(AbrechnungMast abrechnungMast);

    @InheritConfiguration
    void updateEntity(AbrechnungMast abrechnungMast, @MappingTarget AbrechnungMastEntity abrechnungMastEntity);

}
