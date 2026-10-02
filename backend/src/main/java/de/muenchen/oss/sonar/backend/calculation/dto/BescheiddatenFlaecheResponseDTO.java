package de.muenchen.oss.sonar.backend.calculation.dto;

import java.math.BigDecimal;

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
