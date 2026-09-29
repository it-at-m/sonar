package de.muenchen.oss.sonar.backend.widerspruch;

import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WiderspruchEntityMapper {

    Widerspruch toWiderspruch(WiderspruchEntity widerspruchEntity);

    @Mapping(target = "id", ignore = true)
    WiderspruchEntity toEntity(Widerspruch widerspruch);

}
