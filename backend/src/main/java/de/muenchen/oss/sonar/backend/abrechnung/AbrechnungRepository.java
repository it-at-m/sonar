package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AbrechnungRepository extends CrudRepository<AbrechnungEntity, UUID> {

    @Query(
        "select abrechnung from AbrechnungEntity abrechnung where abrechnung.projektId = :projektId "
                + "and not exists (select nachfolger.id from AbrechnungEntity nachfolger "
                + "where nachfolger.vorgaengerAbrechnungId = abrechnung.id)"
    )
    Page<AbrechnungEntity> findNewestVersionsByProjektId(@Param("projektId") UUID projektId, Pageable pageable);

    List<AbrechnungVersion> findByProjektId(UUID projektId);

    Optional<AbrechnungEntity> findByIdAndProjektId(UUID id, UUID projektId);

    boolean existsByVorgaengerAbrechnungId(UUID vorgaengerAbrechnungId);

}
