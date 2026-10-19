package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Mast(
        int nummer,
        LocalDate beginn,
        LocalDate ende,
        int ersterMonat) {

    public int dauerMonate() {
        return (int) ChronoUnit.MONTHS.between(beginn, ende) + 1;
    }

    public int letzterMonat() {
        return ersterMonat + dauerMonate() - 1;
    }

    public boolean isActive(final int monat) {
        return monat >= ersterMonat && monat <= letzterMonat();
    }

    public int runningMonth(final int monat) {
        return monat - ersterMonat + 1;
    }
}
