package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechengroessenstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * What a Woche is charged on under one Flaechenzeitindex, with and without the Aufschlag. Those two
 * decide the Gebührensatz, so the Flächen of all Adressen are gathered by them before anything is
 * charged.
 */
public record Gebuehrenposten(
        Flaechengroessenstufe flaechengroessenstufe,
        boolean isAufschlag50Prozent,
        Flaechenzeitindex flaechenzeitindex,
        BigDecimal flaecheQm) {

    /** A started square metre is charged as a whole one. */
    public int flaecheGerundetQm() {
        return flaecheQm.setScale(0, RoundingMode.CEILING).intValueExact();
    }

    public BigDecimal gebuehrensatz() {
        return flaechengroessenstufe.gebuehrensatz(flaechenzeitindex, isAufschlag50Prozent);
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
