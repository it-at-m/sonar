package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Gebuehrenstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Zeitindex;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * What a Woche is charged on under one Zeitindex, with and without the Aufschlag. Those two decide
 * the Gebührensatz, so the Flächen of all Adressen are gathered by them before anything is charged.
 */
public record Gebuehrenposten(
        Gebuehrenstufe gebuehrenstufe,
        boolean isAufschlag50Prozent,
        Zeitindex zeitindex,
        BigDecimal flaecheQm) {

    /** A started square metre is charged as a whole one. */
    public int flaecheGerundetQm() {
        return flaecheQm.setScale(0, RoundingMode.CEILING).intValueExact();
    }

    public BigDecimal gebuehrensatz() {
        return gebuehrenstufe.gebuehrensatz(zeitindex, isAufschlag50Prozent);
    }

    public BigDecimal gebuehr() {
        return gebuehrFuer(1);
    }

    public BigDecimal gebuehrFuer(final int wochen) {
        return BigDecimal.valueOf(wochen)
                .multiply(BigDecimal.valueOf(flaecheGerundetQm()))
                .multiply(gebuehrensatz());
    }
}
