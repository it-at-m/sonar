package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Mastzeitindex;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public record Mast(
        int nummer,
        LocalDate beginn,
        LocalDate ende,
        int ersterMonat,
        int letzterMonat) {

    public static Mast from(final AbrechnungMast mast, final int nummer, final LocalDate beginnAdresse) {
        final int ersterMonat = (int) ChronoUnit.MONTHS.between(beginnAdresse, mast.beginn()) + 1;
        return new Mast(nummer, mast.beginn(), mast.ende(), ersterMonat, ersterMonat + monthsInclusive(mast) - 1);
    }

    private static int monthsInclusive(final AbrechnungMast mast) {
        return (int) ChronoUnit.MONTHS.between(mast.beginn(), mast.ende()) + 1;
    }

    public int dauerMonate() {
        return letzterMonat - ersterMonat + 1;
    }

    public boolean isActive(final int monat) {
        return monat >= ersterMonat && monat <= letzterMonat;
    }

    public int runningMonth(final int monat) {
        return monat - ersterMonat + 1;
    }

    public Optional<String> zwischenabrechnungGrund(final int monat) {

        final Mastzeitindex mastzeitindex = Mastzeitindex.from(runningMonth(monat));

        if (mastzeitindex == Mastzeitindex.from(runningMonth(monat) - 1)) {
            return Optional.empty();
        }
        return Optional.of("Mast " + nummer + " wechselt in " + mastzeitindex.getBezeichnung());
    }
}
