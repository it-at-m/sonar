package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.BescheiddatenFlaeche;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadressabrechnung;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** This record is temporary. It exists for the summary the UI shows, and will be removed. */
public record Berechnungsergebnis(
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
        List<Ueberspannungsadressabrechnung> abrechnungenUeberspannungen) {

    /**
     * A missing list becomes an empty one: MapStruct maps a null collection to null, and
     * {@link List#copyOf} would reject it.
     */
    public Berechnungsergebnis {
        bescheiddatenFlaechen = bescheiddatenFlaechen == null ? List.of() : List.copyOf(bescheiddatenFlaechen);
        abrechnungenUeberspannungen = abrechnungenUeberspannungen == null ? List.of() : List.copyOf(abrechnungenUeberspannungen);
    }
}
