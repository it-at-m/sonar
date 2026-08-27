package de.muenchen.oss.sonar.backend.projekt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@AbrechnungszeitraumOrdered
public record ProjektRequestDTO(
        @NotNull @Size(min = 1, max = 20) String projektnummer,
        @NotNull LocalDate abrechnungBeginn,
        @NotNull LocalDate abrechnungEnde,
        @NotEmpty List<@Valid ProjektAdresseRequestDTO> adressen) {

    /**
     * Copies the addresses in, so that the request stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one, which {@code @NotEmpty} rejects just the same.
     */
    public ProjektRequestDTO {
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
    }
}
