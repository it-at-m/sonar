package de.muenchen.oss.sonar.backend.berechnung.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BerechnungRequestDTO(
        @NotNull @Size(min = 1, max = 20) String projektnummer,
        @NotEmpty List<@Valid BerechnungNutzungsobjektRequestDTO> nutzungsobjekte) {

    public BerechnungRequestDTO {
        nutzungsobjekte = nutzungsobjekte == null ? List.of() : List.copyOf(nutzungsobjekte);
    }
}
