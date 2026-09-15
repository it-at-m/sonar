package de.muenchen.oss.sonar.backend.berechnung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BerechnungNutzungsobjektRequestDTO(
        @NotNull UUID nutzungsobjektId,
        // The Adresse or Flurstück written as one line, the way it reads on screen. The bound is above
        // the sum of the columns the line is built from, none of which is stored here.
        @NotNull @Size(min = 1, max = 1000) String adressbezeichnung,
        @NotEmpty List<@Valid BerechnungPositionRequestDTO> positionen) {

    /**
     * Copies the positions in, so that the request stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one, which {@code @NotEmpty} rejects just the same.
     */
    public BerechnungNutzungsobjektRequestDTO {
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
    }
}
