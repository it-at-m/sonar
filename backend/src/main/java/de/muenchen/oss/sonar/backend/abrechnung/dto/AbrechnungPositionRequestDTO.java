package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.NotZero;
import de.muenchen.oss.sonar.backend.common.ZeitraumOrdered;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@ZeitraumOrdered(von = "beginn", bis = "ende", message = "Das Ende einer Position darf nicht vor deren Beginn liegen.")
public record AbrechnungPositionRequestDTO(
        @Size(max = 255) String bezeichnung,
        @NotNull LocalDate beginn,
        @NotNull LocalDate ende,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal laenge,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal breite,
        @NotNull @NotZero(message = "Die Fläche einer Position darf nicht 0 sein.") @Digits(integer = 10, fraction = 2) BigDecimal flaeche,
        boolean haelfte,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal anteilAnFlaeche) {
}
