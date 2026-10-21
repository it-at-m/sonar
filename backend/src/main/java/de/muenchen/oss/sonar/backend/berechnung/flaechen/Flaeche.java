package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Flaeche(
        String bezeichnung,
        LocalDate beginn,
        LocalDate berechnungsEnde,
        int ersteWoche,
        BigDecimal gesamtflaecheAdresse,
        BigDecimal flaechenaenderung) {

    private static final int TAGE_PRO_WOCHE = 7;

    public int dauerTage() {
        return (int) ChronoUnit.DAYS.between(beginn, berechnungsEnde) + 1;
    }

    public int angefangeneWochen() {
        return (int) Math.ceil((double) dauerTage() / TAGE_PRO_WOCHE);
    }

    public int letzteWoche() {
        return ersteWoche + angefangeneWochen() - 1;
    }

    public boolean isInAbrechnungszeitraum() {
        return !beginn.isAfter(berechnungsEnde);
    }

    public boolean isLongerThanOneDay() {
        return berechnungsEnde.isAfter(beginn);
    }
}
