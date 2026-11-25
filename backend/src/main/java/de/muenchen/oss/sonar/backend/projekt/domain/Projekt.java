package de.muenchen.oss.sonar.backend.projekt.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Used on the way in and on the way out, so the id is empty until the Projekt is persisted. The
 * linked Projekte are likewise empty on the way in.
 *
 * @param vorgaengerProjekte all Projekte before this one, from the first one to the direct
 *            Vorgaenger
 * @param nachfolgerProjekte all Projekte after this one, from the direct Nachfolger to the last one
 * @param verknuepfungAbgelehnt whether someone decided that this Projekt gets no Vorgaenger
 * @param verknuepfungMoeglich whether this Projekt is unlinked and another Projekt
 *            without Nachfolger shares an Adresse with it, so that it can become its Vorgaenger
 */
public record Projekt(
        UUID id,
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        BigDecimal verwaltungsgebuehr,
        List<ProjektAdresse> adressen,
        UUID vorgaengerProjektId,
        List<LinkedProjekt> vorgaengerProjekte,
        List<LinkedProjekt> nachfolgerProjekte,
        boolean verknuepfungAbgelehnt,
        boolean verknuepfungMoeglich) {

    // TODO Store the Verwaltungsgebuehr on the Projekt. Until then every Projekt is charged the same amount.
    public static final BigDecimal VERWALTUNGSGEBUEHR = new BigDecimal("42");

    /**
     * Copies the addresses and the linked Projekte in, so that the Projekt stays immutable
     * however the caller treats the lists it passed. A missing list becomes an empty one: MapStruct
     * maps a null collection to null, and {@link List#copyOf} would reject it.
     */
    public Projekt {
        if (!Zeitraum.isOrdered(abrechnungBeginn, abrechnungEnde)) {
            throw new IllegalArgumentException("abrechnungEnde is before abrechnungBeginn");
        }
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
        vorgaengerProjekte = vorgaengerProjekte == null ? List.of() : List.copyOf(vorgaengerProjekte);
        nachfolgerProjekte = nachfolgerProjekte == null ? List.of() : List.copyOf(nachfolgerProjekte);
    }
}
