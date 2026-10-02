package de.muenchen.oss.sonar.backend.calculation.dto;

import de.muenchen.oss.sonar.backend.calculation.Calculation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalculationDTOMapper {

    CalculationResponseDTO toDTO(Calculation calculation);

    List<CalculationResponseDTO> toDTOs(List<Calculation> calculations);

}
