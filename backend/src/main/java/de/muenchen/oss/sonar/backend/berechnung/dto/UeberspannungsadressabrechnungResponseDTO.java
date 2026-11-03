package de.muenchen.oss.sonar.backend.berechnung.dto;

import java.math.BigDecimal;
import java.util.List;

// This record is temporary. It exists for the summary the UI shows, and will be removed.
public record UeberspannungsadressabrechnungResponseDTO(
        String adressbezeichnung,
        String berechnungslog,
        BigDecimal gebuehr,
        List<BescheiddatenUeberspannungResponseDTO> abschnitte) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public UeberspannungsadressabrechnungResponseDTO {
        abschnitte = abschnitte == null ? List.of() : List.copyOf(abschnitte);
    }
}
