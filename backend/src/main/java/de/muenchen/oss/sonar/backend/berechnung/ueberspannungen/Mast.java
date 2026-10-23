package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Mast(
        int nummer,
        LocalDate beginn,
        LocalDate berechnungsEnde,
        int ersterMonat) {

    public int dauerMonate() {
        return (int) ChronoUnit.MONTHS.between(beginn, berechnungsEnde) + 1;
    }

    public int letzterMonat() {
        return ersterMonat + dauerMonate() - 1;
    }

    public boolean isInAbrechnungszeitraum() {
        return !beginn.isAfter(berechnungsEnde);
    }

    public boolean isAktivIn(final int monat) {
        return isInAbrechnungszeitraum() && monat >= ersterMonat && monat <= letzterMonat();
    }

    public int alterInMonaten(final int monat) {
        return monat - ersterMonat + 1;
    }
}
