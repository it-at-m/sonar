package de.muenchen.oss.sonar.backend.abrechnung.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AbrechnungPositionResponseDTO(
        UUID id,
        LocalDate beginn,
        LocalDate ende,
        BigDecimal laenge,
        BigDecimal breite,
        BigDecimal flaeche,
        boolean haelfte,
        BigDecimal anteilAnFlaeche) {
}
