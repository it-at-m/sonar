package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.Nutzung;
import de.muenchen.oss.sonar.backend.abrechnung.NutzungsobjektArt;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzungValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@NutzungsobjektArtValid
@UnerlaubteNutzungValid
public record AbrechnungNutzungsobjektRequestDTO(
        @NotNull NutzungsobjektArt art,
        @Size(min = 1, max = 255) String adresse,
        @Size(min = 1, max = 20) String hausnummerVon,
        @Size(min = 1, max = 20) String hausnummerBis,
        @Size(min = 1, max = 255) String flurstueck,
        @Size(min = 1, max = 255) String gemarkung,
        Nutzung nutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        @Min(1) Integer tageUnerlaubteNutzung,
        @Size(max = 10_000) String bemerkung,
        @NotEmpty List<@Valid AbrechnungPositionRequestDTO> positionen) implements UnerlaubteNutzung {

    /**
     * Copies the positions in, so that the request stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one, which {@code @NotEmpty} rejects just the same.
     */
    public AbrechnungNutzungsobjektRequestDTO {
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
    }
}
