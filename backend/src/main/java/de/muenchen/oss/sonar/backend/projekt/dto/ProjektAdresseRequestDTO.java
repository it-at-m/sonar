package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressartValid;
import de.muenchen.oss.sonar.backend.common.Adresse;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzungValid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@AdressartValid
@UnerlaubteNutzungValid
public record ProjektAdresseRequestDTO(
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
        @NotNull @Min(0) Integer anzahlMahnungen,
        boolean sondernutzungErlaubt) implements Adresse, UnerlaubteNutzung {
}
