package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungEntityMapper {

    @Mapping(target = "neuereVersionVorhanden", source = "neuereVersionVorhanden")
    @Mapping(target = "nutzungsobjekte", source = "abrechnungEntity")
    Abrechnung toAbrechnung(AbrechnungEntity abrechnungEntity, boolean neuereVersionVorhanden);

    /**
     * Filters the Positionen of this Abrechnung below the Nutzungsobjekt they name. Several
     * Abrechnungen bill the same Nutzungsobjekt, and each of them shows only its own Positionen.
     */
    default List<AbrechnungNutzungsobjekt> toNutzungsobjekte(final AbrechnungEntity abrechnungEntity) {
        final Map<UUID, List<AbrechnungPosition>> positionenJeNutzungsobjekt = abrechnungEntity.getPositionen().stream()
                .collect(Collectors.groupingBy(position -> position.getNutzungsobjekt().getId(),
                        Collectors.mapping(this::toPosition, Collectors.toList())));
        return abrechnungEntity.getNutzungsobjekte().stream()
                .map(nutzungsobjekt -> toNutzungsobjekt(nutzungsobjekt,
                        positionenJeNutzungsobjekt.getOrDefault(nutzungsobjekt.getId(), List.of())))
                .toList();
    }

    @Mapping(target = ".", source = "nutzungsobjektEntity.adressdaten")
    AbrechnungNutzungsobjekt toNutzungsobjekt(NutzungsobjektEntity nutzungsobjektEntity,
            List<AbrechnungPosition> positionen);

    AbrechnungPosition toPosition(AbrechnungPositionEntity abrechnungPositionEntity);

    default AbrechnungEntity toEntity(final Abrechnung abrechnung,
            final Map<UUID, NutzungsobjektEntity> selectedPreexistingNutzungsobjekte) {
        final AbrechnungEntity abrechnungEntity = toEntity(abrechnung);
        for (final AbrechnungNutzungsobjekt nutzungsobjekt : abrechnung.nutzungsobjekte()) {
            final NutzungsobjektEntity nutzungsobjektEntity = nutzungsobjekt.id() == null
                    ? toEntity(nutzungsobjekt)
                    : selectedPreexistingNutzungsobjekte.get(nutzungsobjekt.id());
            abrechnungEntity.addNutzungsobjekt(nutzungsobjektEntity);
            for (final AbrechnungPosition position : nutzungsobjekt.positionen()) {
                final AbrechnungPositionEntity positionEntity = toEntity(position);
                positionEntity.setNutzungsobjekt(nutzungsobjektEntity);
                abrechnungEntity.addPosition(positionEntity);
            }
        }
        return abrechnungEntity;
    }

    /**
     * The place in the chain of versions is left to the service. It follows from the Vorgänger and
     * never from the data that was entered.
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
