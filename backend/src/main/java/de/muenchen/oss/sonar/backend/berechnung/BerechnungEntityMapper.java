package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.BescheiddatenFlaeche;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.BescheiddatenUeberspannung;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadressabrechnung;
import java.util.UUID;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** This mapper is temporary. It exists for the summary the UI shows, and will be removed. */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BerechnungEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "abrechnungId", source = "abrechnungId")
    BerechnungEntity toEntity(Berechnungsergebnis berechnung, UUID abrechnungId);

    @Mapping(target = "id", ignore = true)
    BerechnungBescheiddatenFlaecheEntity toEntity(BescheiddatenFlaeche bescheiddatenFlaeche);

    @Mapping(target = "id", ignore = true)
    BerechnungUeberspannungEntity toEntity(Ueberspannungsadressabrechnung ueberspannungsadressabrechnung);

    @Mapping(target = "id", ignore = true)
    BerechnungBescheiddatenUeberspannungEntity toEntity(BescheiddatenUeberspannung bescheiddatenUeberspannung);

    Berechnungsergebnis toBerechnungsergebnis(BerechnungEntity berechnungEntity);

    BescheiddatenFlaeche toBescheiddatenFlaeche(BerechnungBescheiddatenFlaecheEntity bescheiddatenEntity);

    Ueberspannungsadressabrechnung toUeberspannungsadressabrechnung(BerechnungUeberspannungEntity ueberspannungenEntity);

    BescheiddatenUeberspannung toBescheiddatenUeberspannung(BerechnungBescheiddatenUeberspannungEntity bescheiddatenEntity);

}
