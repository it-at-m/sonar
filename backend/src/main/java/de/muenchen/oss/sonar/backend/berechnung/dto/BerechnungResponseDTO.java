package de.muenchen.oss.sonar.backend.berechnung.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// This record is temporary. It exists for the summary the UI shows, and will be removed.
public record BerechnungResponseDTO(
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
        List<UeberspannungsadressabrechnungResponseDTO> abrechnungenUeberspannungen) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public BerechnungResponseDTO {
        bescheiddatenFlaechen = bescheiddatenFlaechen == null ? List.of() : List.copyOf(bescheiddatenFlaechen);
        abrechnungenUeberspannungen = abrechnungenUeberspannungen == null ? List.of() : List.copyOf(abrechnungenUeberspannungen);
    }
}
