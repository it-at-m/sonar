package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressartValid;
import de.muenchen.oss.sonar.backend.common.Adresse;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzungValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@AdressartValid
@UnerlaubteNutzungValid
public record AbrechnungNutzungsobjektRequestDTO(
        @NotNull Adressart art,
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
        boolean aufschlag50prozent,
        List<@Valid AbrechnungPositionRequestDTO> positionen,
        List<@Valid AbrechnungMastRequestDTO> masten) implements Adresse, UnerlaubteNutzung {

    /**
     * Copies the Positionen and the Masten in, so that the request stays immutable however the caller
     * treats the lists it passed. A missing list becomes an empty one, because a Nutzungsobjekt can
     * carry Flächen, Masten or both.
     */
    public AbrechnungNutzungsobjektRequestDTO {
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
        masten = masten == null ? List.of() : List.copyOf(masten);
    }
}
