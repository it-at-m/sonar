package de.muenchen.oss.sonar.backend.projekt.model;

import java.time.LocalDate;
import java.util.List;

public record CreateProjektCommand(
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        List<Adresse> adressen) {

    /**
     * Copies the addresses in, so that the command stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public CreateProjektCommand {
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
    }

    public record Adresse(
            String bezeichnung,
            String baunutzung,
            LocalDate unerlaubteNutzungVon,
            LocalDate unerlaubteNutzungBis,
            Integer tageUnerlaubteNutzung,
            Integer anzahlMahnungen,
            boolean sondernutzungErlaubt) {
    }
}
