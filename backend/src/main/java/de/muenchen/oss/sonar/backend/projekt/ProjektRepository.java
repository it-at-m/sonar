package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjektRepository extends CrudRepository<ProjektEntity, UUID>, JpaSpecificationExecutor<ProjektEntity> {

    boolean existsByIdAndStadtbezirkIn(UUID id, Collection<Stadtbezirk> stadtbezirke);
}
