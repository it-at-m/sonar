package de.muenchen.oss.sonar.backend.abrechnung.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AbrechnungMastResponseDTO(
        UUID id,
        LocalDate beginn,
        LocalDate ende) {
}
