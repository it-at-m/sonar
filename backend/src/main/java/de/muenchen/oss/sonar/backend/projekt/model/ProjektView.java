package de.muenchen.oss.sonar.backend.projekt.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjektView(
        UUID id,
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        List<ProjektAdresseView> adressen) {

    /**
     * Copies the addresses in, so that the view stays immutable however the caller treats the list it
     * passed. A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public ProjektView {
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
    }
}
