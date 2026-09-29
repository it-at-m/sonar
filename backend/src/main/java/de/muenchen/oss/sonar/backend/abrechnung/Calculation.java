package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.berechnung.abrechnung.AbrechnungUeberspannungen;
import de.muenchen.oss.sonar.backend.berechnung.abrechnung.BescheiddatenFlaeche;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Calculation {

    private final int lfdNr;
    private final LocalDate abrechnungszeitraumVon;
    private final LocalDate abrechnungszeitraumBis;
    private final List<BescheiddatenFlaeche> bescheiddatenFlaechen = new ArrayList<>();
    private final List<AbrechnungUeberspannungen> abrechnungenUeberspannungen = new ArrayList<>();
    private String berechnungslog = "";
    @Setter
    private String zusammenfassung = "";
    @Setter
    private BigDecimal gebuehrFlaechen = BigDecimal.ZERO;
    @Setter
    private BigDecimal gebuehrUeberspannungen = BigDecimal.ZERO;
    @Setter
    private BigDecimal gebuehrVerwaltung = BigDecimal.ZERO;
    @Setter
    private BigDecimal gebuehrGesamt = BigDecimal.ZERO;
    @Setter
    private BigDecimal gebuehrZahlung = BigDecimal.ZERO;

    public void addBescheiddatenFlaeche(final BescheiddatenFlaeche bescheiddaten) {
        bescheiddatenFlaechen.add(bescheiddaten);
    }

    public void addAbrechnungUeberspannungen(final AbrechnungUeberspannungen abrechnungUeberspannungen) {
        abrechnungenUeberspannungen.add(abrechnungUeberspannungen);
    }

    public List<BescheiddatenFlaeche> getBescheiddatenFlaechen() {
        return Collections.unmodifiableList(bescheiddatenFlaechen);
    }

    public List<AbrechnungUeberspannungen> getAbrechnungenUeberspannungen() {
        return Collections.unmodifiableList(abrechnungenUeberspannungen);
    }

    public void appendToBerechnungslog(final String text) {
        berechnungslog = berechnungslog.concat(text);
    }

}
