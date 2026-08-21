package de.muenchen.oss.sonar.backend.projekt.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjektResponseDTO(
        UUID id,
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        List<ProjektAdresseResponseDTO> adressen) {

    /**
     * Copies the addresses in, so that the response stays immutable however the caller treats the list
     * it passed. A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public ProjektResponseDTO {
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
    }
}
