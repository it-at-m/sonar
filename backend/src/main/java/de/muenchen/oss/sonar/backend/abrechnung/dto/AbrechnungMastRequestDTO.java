package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.ZeitraumOrdered;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@ZeitraumOrdered(von = "beginn", bis = "ende", message = "Das Ende eines Masten darf nicht vor dessen Beginn liegen.")
public record AbrechnungMastRequestDTO(
        UUID id,
        @NotNull LocalDate beginn,
        @NotNull LocalDate ende) {
}
