package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AbrechnungNutzungsobjektResponseDTO(
        UUID id,
        Adressart art,
        String adresse,
        String hausnummerVon,
        String hausnummerBis,
        String flurstueck,
        String gemarkung,
        Nutzung nutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        Integer tageUnerlaubteNutzung,
        String bemerkung,
        List<AbrechnungPositionResponseDTO> positionen,
        List<AbrechnungMastResponseDTO> masten) {

    /**
     * Copies the positions and the Masten in, so that the response stays immutable however the caller
     * treats the lists it passed. A missing list becomes an empty one: MapStruct maps a null
     * collection to null, and {@link List#copyOf} would reject it.
     */
    public AbrechnungNutzungsobjektResponseDTO {
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
        masten = masten == null ? List.of() : List.copyOf(masten);
    }
}
