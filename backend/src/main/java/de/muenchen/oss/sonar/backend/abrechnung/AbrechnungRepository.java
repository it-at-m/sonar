package de.muenchen.oss.sonar.backend.abrechnung;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbrechnungRepository extends CrudRepository<AbrechnungEntity, UUID> {

    Page<AbrechnungEntity> findByProjektId(UUID projektId, Pageable pageable);

}
