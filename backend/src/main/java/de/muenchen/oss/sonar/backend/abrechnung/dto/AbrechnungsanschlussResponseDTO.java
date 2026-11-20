package de.muenchen.oss.sonar.backend.abrechnung.dto;

import java.time.LocalDate;

public record AbrechnungsanschlussResponseDTO(boolean anlegenMoeglich, LocalDate zeitraumVon) {
}
