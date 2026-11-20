package de.muenchen.oss.sonar.backend.abrechnung;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NutzungsobjektRepository extends CrudRepository<NutzungsobjektEntity, UUID> {

    @Query(
        "select nutzungsobjekt from NutzungsobjektEntity nutzungsobjekt where nutzungsobjekt.id in :ids "
                + "and not exists (select abrechnung.id from AbrechnungEntity abrechnung "
                + "join abrechnung.nutzungsobjekte billed where billed.id = nutzungsobjekt.id)"
    )
    List<NutzungsobjektEntity> findUnbilledByIdIn(@Param("ids") Collection<UUID> ids);

}
