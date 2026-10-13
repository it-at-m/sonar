package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
import java.math.BigDecimal;
import java.util.Optional;

public record Flaechenbeitrag(Flaeche flaeche, BigDecimal flaecheQm, int woche, int letzteWocheAdresse) {

    private static final String KEIN_FLAECHENZEITINDEX = "";

    public boolean isAktiv() {
        return isAktivIn(woche);
    }

    public Optional<Flaechenzeitindex> flaechenzeitindex() {
        return flaechenzeitindexIn(woche);
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

        final Optional<Flaechenzeitindex> flaechenzeitindex = flaechenzeitindexIn(woche);
        final Optional<Flaechenzeitindex> flaechenzeitindexVorwoche = flaechenzeitindexIn(woche - 1);

        if (flaechenzeitindex.equals(flaechenzeitindexVorwoche)) {
            return Optional.empty();
        }
        if (!isAktivIn(woche) && woche > letzteWocheAdresse) {
            return Optional.of("Fläche nicht mehr aktiv (Fläche " + flaeche.bezeichnung() + ")");
        }
        return Optional.of("Wechsel von " + bezeichnung(flaechenzeitindexVorwoche) + " zu " + bezeichnung(flaechenzeitindex)
                + " (Fläche " + flaeche.bezeichnung() + ")");
    }

    private boolean isAktivIn(final int woche) {
        return woche >= flaeche.ersteWoche()
                && woche <= letzteWocheAdresse
                && flaecheQm.signum() != 0
                && flaeche.isImAbrechnungszeitraum();
    }

    private Optional<Flaechenzeitindex> flaechenzeitindexIn(final int woche) {
        return isAktivIn(woche)
                ? Optional.of(Flaechenzeitindex.from(woche - flaeche.ersteWoche()))
                : Optional.empty();
    }

    private String flaechenaenderung() {
        final BigDecimal aenderung = flaeche.flaechenaenderung();
        final String art = aenderung.signum() > 0 ? "Flächenerweiterung +" : "Flächenreduzierung ";
        return art + aenderung + "m² (Fläche " + flaeche.bezeichnung() + ")";
    }

    private static String bezeichnung(final Optional<Flaechenzeitindex> flaechenzeitindex) {
        return flaechenzeitindex.map(Flaechenzeitindex::getBezeichnung).orElse(KEIN_FLAECHENZEITINDEX);
    }
}
