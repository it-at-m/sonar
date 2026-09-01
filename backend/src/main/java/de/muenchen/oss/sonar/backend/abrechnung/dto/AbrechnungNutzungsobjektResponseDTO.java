package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.Nutzung;
import de.muenchen.oss.sonar.backend.abrechnung.NutzungsobjektArt;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AbrechnungNutzungsobjektResponseDTO(
        UUID id,
        NutzungsobjektArt art,
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
        List<AbrechnungPositionResponseDTO> positionen) {

    /**
     * Copies the positions in, so that the response stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public AbrechnungNutzungsobjektResponseDTO {
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
    }
}
