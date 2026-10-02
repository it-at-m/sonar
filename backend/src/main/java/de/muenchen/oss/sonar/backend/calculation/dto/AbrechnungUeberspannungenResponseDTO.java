package de.muenchen.oss.sonar.backend.calculation.dto;

import java.math.BigDecimal;
import java.util.List;

public record AbrechnungUeberspannungenResponseDTO(
        String adresse,
        String berechnungslog,
        BigDecimal gebuehrUeberspannungen,
        List<BescheiddatenUeberspannungResponseDTO> bescheiddatenUeberspannungen) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public AbrechnungUeberspannungenResponseDTO {
        bescheiddatenUeberspannungen = bescheiddatenUeberspannungen == null ? List.of() : List.copyOf(bescheiddatenUeberspannungen);
    }
}
