package de.muenchen.oss.sonar.backend.calculation;

import de.muenchen.oss.sonar.backend.berechnung.abrechnung.AbrechnungUeberspannungen;
import de.muenchen.oss.sonar.backend.berechnung.abrechnung.BescheiddatenFlaeche;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder(builderClassName = "Builder")
public record CalculationResult(
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
        List<BescheiddatenFlaeche> bescheiddatenFlaechen,
        List<AbrechnungUeberspannungen> abrechnungenUeberspannungen) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public CalculationResult {
        bescheiddatenFlaechen = bescheiddatenFlaechen == null ? List.of() : List.copyOf(bescheiddatenFlaechen);
        abrechnungenUeberspannungen = abrechnungenUeberspannungen == null ? List.of() : List.copyOf(abrechnungenUeberspannungen);
    }
}
