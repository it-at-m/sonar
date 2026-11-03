package de.muenchen.oss.sonar.backend.berechnung.dto;

import java.math.BigDecimal;

// This record is temporary. It exists for the summary the UI shows, and will be removed.
public record BescheiddatenUeberspannungResponseDTO(
        int monatBeginn,
        int monatEnde,
        int monatDauer,
        String grund,
        String berechnung,
        BigDecimal gebuehr) {
}
