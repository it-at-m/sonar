package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.ZeitraumOrdered;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@ZeitraumOrdered(von = "beginn", bis = "ende", message = "Das Ende einer Position darf nicht vor deren Beginn liegen.")
public record AbrechnungPositionRequestDTO(
        @NotNull LocalDate beginn,
        @NotNull LocalDate ende,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal laenge,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal breite,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal flaeche,
        boolean haelfte,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal anteilAnFlaeche) {
}
