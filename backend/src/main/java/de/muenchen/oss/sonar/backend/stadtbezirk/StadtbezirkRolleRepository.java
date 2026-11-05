package de.muenchen.oss.sonar.backend.stadtbezirk;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadtbezirkRolleRepository extends CrudRepository<StadtbezirkRolleEntity, UUID> {

    List<StadtbezirkRolleEntity> findByRolleIn(Collection<String> rollen);

}
