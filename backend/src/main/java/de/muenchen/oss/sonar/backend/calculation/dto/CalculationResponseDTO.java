package de.muenchen.oss.sonar.backend.calculation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CalculationResponseDTO(
        int lfdNr,
        LocalDate abrechnungszeitraumVon,
        LocalDate abrechnungszeitraumBis,
        String zusammenfassung,
        String berechnungslog,
        BigDecimal gebuehrFlaechen,
        BigDecimal gebuehrUeberspannungen,
        BigDecimal gebuehrVerwaltung,
        BigDecimal gebuehrGesamt,
        BigDecimal gebuehrZahlung,
        List<BescheiddatenFlaecheResponseDTO> bescheiddatenFlaechen,
        List<AbrechnungUeberspannungenResponseDTO> abrechnungenUeberspannungen) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public CalculationResponseDTO {
        bescheiddatenFlaechen = bescheiddatenFlaechen == null ? List.of() : List.copyOf(bescheiddatenFlaechen);
        abrechnungenUeberspannungen = abrechnungenUeberspannungen == null ? List.of() : List.copyOf(abrechnungenUeberspannungen);
    }
}
