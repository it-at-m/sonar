package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungVersion;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnungsanschluss;
import de.muenchen.oss.sonar.backend.abrechnung.domain.NutzungsobjektSuggestion;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungDTOMapper {

    AbrechnungResponseDTO toDTO(Abrechnung abrechnung);

    AbrechnungVersionResponseDTO toDTO(AbrechnungVersion abrechnungVersion);

    AbrechnungsanschlussResponseDTO toDTO(Abrechnungsanschluss abrechnungsanschluss);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionsnummer", ignore = true)
    @Mapping(target = "vorgaengerAbrechnungId", ignore = true)
    @Mapping(target = "widerspruch", ignore = true)
    @Mapping(target = "neuereVersionVorhanden", ignore = true)
    Abrechnung toAbrechnung(UUID projektId, AbrechnungRequestDTO abrechnungRequestDTO);

    @Mapping(target = ".", source = "nutzungsobjekt")
    @Mapping(target = "projektnummer", source = "vorgaengerProjektnummer")
    NutzungsobjektResponseDTO toDTO(NutzungsobjektSuggestion nutzungsobjektSuggestion);

    AbrechnungNutzungsobjekt toAbrechnungNutzungsobjekt(AbrechnungNutzungsobjektRequestDTO abrechnungNutzungsobjektRequestDTO);

    @Mapping(target = "id", ignore = true)
    AbrechnungPosition toAbrechnungPosition(AbrechnungPositionRequestDTO abrechnungPositionRequestDTO);

    @Mapping(target = "id", ignore = true)
    AbrechnungMast toAbrechnungMast(AbrechnungMastRequestDTO abrechnungMastRequestDTO);

}
