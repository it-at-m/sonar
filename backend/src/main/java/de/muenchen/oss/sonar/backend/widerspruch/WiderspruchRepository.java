package de.muenchen.oss.sonar.backend.widerspruch;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WiderspruchRepository extends CrudRepository<WiderspruchEntity, UUID> {

    Optional<WiderspruchEntity> findByAbrechnungId(UUID abrechnungId);

    List<WiderspruchEntity> findByAbrechnungIdIn(Collection<UUID> abrechnungIds);

}
