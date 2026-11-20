package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungVersion;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungDTOMapper {

    AbrechnungResponseDTO toDTO(Abrechnung abrechnung);

    AbrechnungVersionResponseDTO toDTO(AbrechnungVersion abrechnungVersion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionsnummer", ignore = true)
    @Mapping(target = "vorgaengerAbrechnungId", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "widerspruch", ignore = true)
    @Mapping(target = "neuereVersionVorhanden", ignore = true)
    Abrechnung toAbrechnung(UUID projektId, AbrechnungRequestDTO abrechnungRequestDTO);

    NutzungsobjektResponseDTO toDTO(AbrechnungNutzungsobjekt abrechnungNutzungsobjekt);

    @Mapping(target = "id", expression = "java(nutzungsobjekt.takenOverId() == null ? nutzungsobjekt.id() : nutzungsobjekt.takenOverId())")
    @Mapping(target = "changeable", expression = "java(nutzungsobjekt.takenOverId() == null)")
    AbrechnungNutzungsobjekt toAbrechnungNutzungsobjekt(AbrechnungNutzungsobjektRequestDTO nutzungsobjekt);

    AbrechnungPosition toAbrechnungPosition(AbrechnungPositionRequestDTO abrechnungPositionRequestDTO);

    AbrechnungMast toAbrechnungMast(AbrechnungMastRequestDTO abrechnungMastRequestDTO);

}
