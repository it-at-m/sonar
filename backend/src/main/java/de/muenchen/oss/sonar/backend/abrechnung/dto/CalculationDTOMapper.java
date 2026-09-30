package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.Calculation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalculationDTOMapper {

    CalculationResponseDTO toDTO(Calculation calculation);

}
