package de.muenchen.oss.sonar.backend.calculation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculationRepository extends CrudRepository<CalculationEntity, UUID> {

    List<CalculationEntity> findByAbrechnungIdOrderByLfdNrAsc(UUID abrechnungId);

    Optional<CalculationEntity> findTopByAbrechnungIdOrderByLfdNrDesc(UUID abrechnungId);

}
