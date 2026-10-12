package de.muenchen.oss.sonar.backend.berechnung.dto;

import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** This mapper is temporary. It exists for the summary the UI shows, and will be removed. */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BerechnungDTOMapper {

    BerechnungResponseDTO toDTO(Berechnungsergebnis berechnung);

}
