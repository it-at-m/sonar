package de.muenchen.oss.sonar.backend.calculation.flaechen;

import de.muenchen.oss.sonar.backend.calculation.gebuehren.Zeitindex;
import java.math.BigDecimal;
import java.util.Optional;

public record Flaechenbeitrag(Flaeche flaeche, BigDecimal flaecheQm, int woche, int letzteWocheAdresse) {

    private static final String KEIN_ZEITINDEX = "";

    public boolean isAktiv() {
        return isAktivIn(woche);
    }

    public Optional<Zeitindex> zeitindex() {
        return zeitindexIn(woche);
    }

    public Optional<String> zwischenabrechnungGrund() {

        if (!flaeche.isLaengerAlsEinTag()) {
            return Optional.empty();
        }

        if (woche == flaeche.ersteWoche()) {
            return Optional.of(flaechenaenderung());
        }

        if (woche <= 1) {
            return Optional.empty();
        }

        final Optional<Zeitindex> zeitindex = zeitindexIn(woche);
        final Optional<Zeitindex> zeitindexVorwoche = zeitindexIn(woche - 1);

        if (zeitindex.equals(zeitindexVorwoche)) {
            return Optional.empty();
        }
        if (!isAktivIn(woche) && woche > letzteWocheAdresse) {
            return Optional.of("Fläche nicht mehr aktiv (Fläche " + flaeche.bezeichnung() + ")");
        }
        return Optional.of("Wechsel von " + bezeichnung(zeitindexVorwoche) + " zu " + bezeichnung(zeitindex)
                + " (Fläche " + flaeche.bezeichnung() + ")");
    }

    private boolean isAktivIn(final int woche) {
        return woche >= flaeche.ersteWoche()
                && woche <= letzteWocheAdresse
                && flaecheQm.signum() != 0
                && flaeche.isImAbrechnungszeitraum();
    }

    private Optional<Zeitindex> zeitindexIn(final int woche) {
        return isAktivIn(woche)
                ? Optional.of(Zeitindex.from(woche - flaeche.ersteWoche()))
                : Optional.empty();
    }

    private String flaechenaenderung() {
        final BigDecimal aenderung = flaeche.flaechenaenderung();
        final String art = aenderung.signum() > 0 ? "Flächenerweiterung +" : "Flächenreduzierung ";
        return art + aenderung + "m² (Fläche " + flaeche.bezeichnung() + ")";
    }

    private static String bezeichnung(final Optional<Zeitindex> zeitindex) {
        return zeitindex.map(Zeitindex::getBezeichnung).orElse(KEIN_ZEITINDEX);
    }
}
