package de.muenchen.oss.sonar.backend.berechnung.dto;

import java.math.BigDecimal;
import java.util.List;

// This record is temporary. It exists for the summary the UI shows, and will be removed.
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
