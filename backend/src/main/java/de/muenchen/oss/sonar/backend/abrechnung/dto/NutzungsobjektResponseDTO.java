package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.time.LocalDate;
import java.util.UUID;

public record NutzungsobjektResponseDTO(
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
        boolean aufschlag50prozent) {
}
