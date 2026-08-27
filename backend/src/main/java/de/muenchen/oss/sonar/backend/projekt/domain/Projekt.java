package de.muenchen.oss.sonar.backend.projekt.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Used on the way in and on the way out, so the id is empty until the Projekt is persisted. */
public record Projekt(
        UUID id,
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        List<ProjektAdresse> adressen) {

    /**
     * Copies the addresses in, so that the Projekt stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public Projekt {
        if (!Zeitraum.isOrdered(abrechnungBeginn, abrechnungEnde)) {
            throw new IllegalArgumentException("abrechnungEnde is before abrechnungBeginn");
        }
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
    }
}
