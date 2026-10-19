package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaechenbeitragService {

    private static final String KEIN_FLAECHENZEITINDEX = "";

    public static Optional<String> zwischenabrechnungGrund(final Flaechenbeitrag beitrag) {

        final Flaeche flaeche = beitrag.flaeche();
        final int woche = beitrag.woche();

        if (!flaeche.isLongerThanOneDay()) {
            return Optional.empty();
        }

        if (woche == flaeche.ersteWoche()) {
            return Optional.of(flaechenaenderung(flaeche));
        }

        if (woche <= 1) {
            return Optional.empty();
        }

        final Optional<Flaechenzeitindex> flaechenzeitindex = beitrag.flaechenzeitindex();
        final Optional<Flaechenzeitindex> flaechenzeitindexVorwoche = beitrag.flaechenzeitindexIn(woche - 1);

        if (flaechenzeitindex.equals(flaechenzeitindexVorwoche)) {
            return Optional.empty();
        }
        if (!beitrag.isAktiv() && woche > beitrag.letzteWocheAdresse()) {
            return Optional.of("Fläche nicht mehr aktiv (Fläche " + flaeche.bezeichnung() + ")");
        }
        return Optional.of("Wechsel von " + bezeichnung(flaechenzeitindexVorwoche) + " zu " + bezeichnung(flaechenzeitindex)
                + " (Fläche " + flaeche.bezeichnung() + ")");
    }

    private static String flaechenaenderung(final Flaeche flaeche) {
        final BigDecimal aenderung = flaeche.flaechenaenderung();
        final String art = aenderung.signum() > 0 ? "Flächenerweiterung +" : "Flächenreduzierung ";
        return art + aenderung + "m² (Fläche " + flaeche.bezeichnung() + ")";
    }

    private static String bezeichnung(final Optional<Flaechenzeitindex> flaechenzeitindex) {
        return flaechenzeitindex.map(Flaechenzeitindex::getBezeichnung).orElse(KEIN_FLAECHENZEITINDEX);
    }
}
