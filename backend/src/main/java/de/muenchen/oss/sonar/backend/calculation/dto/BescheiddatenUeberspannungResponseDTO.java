package de.muenchen.oss.sonar.backend.calculation.dto;

import java.math.BigDecimal;

public record BescheiddatenUeberspannungResponseDTO(
        int monatBeginn,
        int monatEnde,
        int monatDauer,
        String grund,
        String berechnung,
        BigDecimal gebuehr) {
}
