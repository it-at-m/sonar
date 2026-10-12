package de.muenchen.oss.sonar.backend.berechnung;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BerechnungRepository extends CrudRepository<BerechnungEntity, UUID> {

    Optional<BerechnungEntity> findByAbrechnungId(UUID abrechnungId);

    boolean existsByAbrechnungId(UUID abrechnungId);

    /**
     * A new version of an Abrechnung corrects what that Abrechnung charged, so the versions of the
     * given Abrechnung are left out. The chain is walked in both directions, because a version in the
     * middle of it reaches the others only through its Vorgänger and its Nachfolger.
     * <p>
     * The query is native because JPQL has no recursion, and a chain of rows pointing at each other
     * cannot be walked without it.
     * </p>
     * <p>
     * This may become obsolete once the concept of Abrechnung versions is reworked.
     * </p>
     */
    @Query(
            value = "with recursive versionskette as ("
                    + "    select abrechnung.id, abrechnung.vorgaenger_abrechnung_id"
                    + "      from abrechnung where abrechnung.id = :abrechnungId"
                    + "    union"
                    + "    select abrechnung.id, abrechnung.vorgaenger_abrechnung_id"
                    + "      from abrechnung, versionskette"
                    + "     where abrechnung.id = versionskette.vorgaenger_abrechnung_id"
                    + "        or abrechnung.vorgaenger_abrechnung_id = versionskette.id"
                    + ") "
                    + "select berechnung.* from berechnung "
                    + "join abrechnung on abrechnung.id = berechnung.abrechnung_id "
                    + "where abrechnung.projekt_id = :projektId "
                    + "and berechnung.abrechnung_id not in (select id from versionskette) "
                    + "order by berechnung.abrechnungszeitraum_bis desc, berechnung.lfd_nr desc "
                    + "limit 1",
            nativeQuery = true
    )
    Optional<BerechnungEntity> findVorabrechnungOfProjekt(@Param("projektId") UUID projektId, @Param("abrechnungId") UUID abrechnungId);

}
