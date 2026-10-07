package de.muenchen.oss.sonar.backend.calculation.dto;

import de.muenchen.oss.sonar.backend.calculation.CalculationResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalculationDTOMapper {

    CalculationResponseDTO toDTO(CalculationResult calculation);

}
