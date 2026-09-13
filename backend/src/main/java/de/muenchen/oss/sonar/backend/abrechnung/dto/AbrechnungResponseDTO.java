package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.ZustellungsbevollmaechtigterTyp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AbrechnungResponseDTO(
        UUID id,
        UUID projektId,
        String geschaeftspartnerId,
        boolean zustellungsbevollmaechtigterGenutzt,
        String zustellungsbevollmaechtigterId,
        ZustellungsbevollmaechtigterTyp zustellungsbevollmaechtigterTyp,
        LocalDate zeitraumVon,
        LocalDate zeitraumBis,
        AbrechnungsArt abrechnungsArt,
        List<AbrechnungNutzungsobjektResponseDTO> nutzungsobjekte) {

    /**
     * Copies the Nutzungsobjekte in, so that the response stays immutable however the caller treats
     * the list it passed. A missing list becomes an empty one: MapStruct maps a null collection to
     * null, and {@link List#copyOf} would reject it.
     */
    public AbrechnungResponseDTO {
        nutzungsobjekte = nutzungsobjekte == null ? List.of() : List.copyOf(nutzungsobjekte);
    }
}
