package de.muenchen.oss.sonar.backend.berechnung.dto;

import de.muenchen.oss.sonar.backend.common.ZeitraumOrdered;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ZeitraumOrdered(von = "beginn", bis = "ende", message = "Das Ende einer Position darf nicht vor deren Beginn liegen.")
public record BerechnungPositionRequestDTO(
        @NotNull UUID positionId,
        @NotNull LocalDate beginn,
        @NotNull LocalDate ende,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal flaecheQm) {
}
