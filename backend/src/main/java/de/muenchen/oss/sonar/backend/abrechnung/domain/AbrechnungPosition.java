package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.Gebuehrenstufe;
import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AbrechnungPosition(
        UUID id,
        LocalDate beginn,
        LocalDate ende,
        BigDecimal laenge,
        BigDecimal breite,
        BigDecimal flaeche,
        boolean haelfte,
        BigDecimal anteilAnFlaeche) {

    // Prototype: wocheGueltigkeitVon is a fixed placeholder until the Position carries the week its
    // Gueltigkeit starts in.
    private static final int WOCHE_GUELTIGKEIT_VON = 1;

    public AbrechnungPosition {
        if (!Zeitraum.isOrdered(beginn, ende)) {
            throw new IllegalArgumentException("ende is before beginn");
        }
    }

    public Gebuehrenstufe.Zeitindex getZeitindex(final int woche) {
        final int wochenSeitGueltigkeitVon = woche - WOCHE_GUELTIGKEIT_VON;
        if (wochenSeitGueltigkeitVon >= Gebuehrenstufe.Zeitgrenze.ZEITGRENZE_3.getGrenze()) {
            return Gebuehrenstufe.Zeitindex.ZEITINDEX_4;
        }
        if (wochenSeitGueltigkeitVon >= Gebuehrenstufe.Zeitgrenze.ZEITGRENZE_2.getGrenze()) {
            return Gebuehrenstufe.Zeitindex.ZEITINDEX_3;
        }
        if (wochenSeitGueltigkeitVon >= Gebuehrenstufe.Zeitgrenze.ZEITGRENZE_1.getGrenze()) {
            return Gebuehrenstufe.Zeitindex.ZEITINDEX_2;
        }
        return Gebuehrenstufe.Zeitindex.ZEITINDEX_1;
    }
}
