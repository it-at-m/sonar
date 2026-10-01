package de.muenchen.oss.sonar.backend.abrechnung;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalculationEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "abrechnungId", source = "abrechnungId")
    CalculationEntity toEntity(Calculation calculation, UUID abrechnungId);

}
