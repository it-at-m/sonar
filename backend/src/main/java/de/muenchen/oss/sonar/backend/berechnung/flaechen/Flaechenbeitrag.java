package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
import java.math.BigDecimal;
import java.util.Optional;

public record Flaechenbeitrag(Flaeche flaeche, BigDecimal flaecheQm, int woche, int letzteWocheAdresse) {

    public boolean isAktiv() {
        return isAktivIn(woche);
    }

    public boolean isAktivIn(final int woche) {
        return woche >= flaeche.ersteWoche()
                && woche <= letzteWocheAdresse
                && flaecheQm.signum() != 0
                && flaeche.isInAbrechnungszeitraum();
    }

    public Optional<Flaechenzeitindex> flaechenzeitindex() {
        return flaechenzeitindexIn(woche);
    }

    public Optional<Flaechenzeitindex> flaechenzeitindexIn(final int woche) {
        return isAktivIn(woche)
                ? Optional.of(Flaechenzeitindex.from(woche - flaeche.ersteWoche()))
                : Optional.empty();
    }
}
