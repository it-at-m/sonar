package de.muenchen.oss.sonar.backend.projekt.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProjektAdresseResponseDTO(
        UUID id,
        String bezeichnung,
        String baunutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        Integer tageUnerlaubteNutzung,
        Integer anzahlMahnungen,
        boolean sondernutzungErlaubt) {
}
