package de.muenchen.oss.sonar.backend.berechnung.dto;

import java.math.BigDecimal;

// This record is temporary. It exists for the summary the UI shows, and will be removed.
public record BescheiddatenFlaecheResponseDTO(
        int wocheBeginn,
        int wocheEnde,
        int wocheDauer,
        String flaechenVerteilung,
        String grund,
        String gebuehrenstufe,
        String berechnung,
        BigDecimal gebuehr) {
}
