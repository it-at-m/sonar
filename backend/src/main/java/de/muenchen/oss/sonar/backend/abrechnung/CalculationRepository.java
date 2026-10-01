package de.muenchen.oss.sonar.backend.abrechnung;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculationRepository extends CrudRepository<CalculationEntity, UUID> {

    Optional<CalculationEntity> findTopByAbrechnungIdOrderByLfdNrDesc(UUID abrechnungId);

}
