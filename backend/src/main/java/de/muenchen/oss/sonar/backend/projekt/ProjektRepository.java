package de.muenchen.oss.sonar.backend.projekt;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjektRepository extends CrudRepository<ProjektEntity, UUID>, JpaSpecificationExecutor<ProjektEntity> {

    boolean existsByVorgaengerProjektId(UUID vorgaengerProjektId);

    List<ProjektEntity> findByVorgaengerProjektIdIn(Collection<UUID> vorgaengerProjektIds);

    @Query(
        """
                select projekt.id, vorgaenger
                from ProjektEntity projekt join projekt.adressen adresse,
                    ProjektEntity vorgaenger join vorgaenger.adressen vorgaengerAdresse
                where projekt.id in :projektIds
                    and vorgaenger.id <> projekt.id
                    and not exists (select nachfolger.id from ProjektEntity nachfolger where nachfolger.vorgaengerProjektId = vorgaenger.id)
                    and vorgaengerAdresse.adressdaten.art = adresse.adressdaten.art
                    and lower(coalesce(vorgaengerAdresse.adressdaten.adresse, '')) = lower(coalesce(adresse.adressdaten.adresse, ''))
                    and lower(coalesce(vorgaengerAdresse.adressdaten.hausnummerVon, '')) = lower(coalesce(adresse.adressdaten.hausnummerVon, ''))
                    and lower(coalesce(vorgaengerAdresse.adressdaten.hausnummerBis, '')) = lower(coalesce(adresse.adressdaten.hausnummerBis, ''))
                    and lower(coalesce(vorgaengerAdresse.adressdaten.flurstueck, '')) = lower(coalesce(adresse.adressdaten.flurstueck, ''))
                    and lower(coalesce(vorgaengerAdresse.adressdaten.gemarkung, '')) = lower(coalesce(adresse.adressdaten.gemarkung, ''))
                order by vorgaenger.projektnummer desc, vorgaenger.id desc
                """
    )
    List<VorgaengerVorschlag> findVorgaengerVorschlaege(@Param("projektIds") Collection<UUID> projektIds);

}
