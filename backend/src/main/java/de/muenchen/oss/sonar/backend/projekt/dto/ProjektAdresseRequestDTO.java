package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzung;
import de.muenchen.oss.sonar.backend.common.UnerlaubteNutzungValid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@UnerlaubteNutzungValid
public record ProjektAdresseRequestDTO(
        @NotNull @Size(min = 1, max = 255) String bezeichnung,
        @Size(max = 255) String baunutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        @Min(1) Integer tageUnerlaubteNutzung,
        @NotNull @Min(0) Integer anzahlMahnungen,
        boolean sondernutzungErlaubt) implements UnerlaubteNutzung {
}
