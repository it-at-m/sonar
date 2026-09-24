package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AbrechnungDTOMapper {

    AbrechnungResponseDTO toDTO(Abrechnung abrechnung);

    @Mapping(target = "id", ignore = true)
    Abrechnung toAbrechnung(UUID projektId, AbrechnungRequestDTO abrechnungRequestDTO);

    @Mapping(target = "id", ignore = true)
    AbrechnungNutzungsobjekt toAbrechnungNutzungsobjekt(AbrechnungNutzungsobjektRequestDTO abrechnungNutzungsobjektRequestDTO);

    @Mapping(target = "id", ignore = true)
    AbrechnungPosition toAbrechnungPosition(AbrechnungPositionRequestDTO abrechnungPositionRequestDTO);

}
