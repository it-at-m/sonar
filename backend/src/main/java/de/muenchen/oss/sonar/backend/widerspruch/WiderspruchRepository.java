package de.muenchen.oss.sonar.backend.widerspruch;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WiderspruchRepository extends CrudRepository<WiderspruchEntity, UUID> {

    boolean existsByAbrechnungId(UUID abrechnungId);

    @Query("select widerspruch.abrechnungId from WiderspruchEntity widerspruch where widerspruch.abrechnungId in :abrechnungIds")
    Set<UUID> findAbrechnungIdsWithWiderspruch(@Param("abrechnungIds") Collection<UUID> abrechnungIds);

}
