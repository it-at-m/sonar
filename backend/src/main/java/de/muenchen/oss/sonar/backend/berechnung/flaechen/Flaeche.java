package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Flaeche(
        String bezeichnung,
        LocalDate beginn,
        LocalDate berechnungsEnde,
        int ersteWoche,
        int letzteWoche,
        BigDecimal gesamtflaecheAdresse,
        BigDecimal flaechenaenderung) {

    private static final int TAGE_PRO_WOCHE = 7;

    public static Flaeche from(final AbrechnungPosition position, final int flaechenNummer, final LocalDate berechnungsdatum,
            final LocalDate beginnWochenlauf, final BigDecimal gesamtflaecheVorher) {

        final LocalDate berechnungsEnde = notAfter(position.ende(), berechnungsdatum);
        final BigDecimal gesamtflaecheAdresse = withoutTrailingZeros(position.flaeche());
        final int ersteWoche = firstWeek(position.beginn(), beginnWochenlauf);

        return new Flaeche(
                "Position " + flaechenNummer,
                position.beginn(),
                berechnungsEnde,
                ersteWoche,
                ersteWoche + startedWeeks(position.beginn(), berechnungsEnde) - 1,
                gesamtflaecheAdresse,
                gesamtflaecheAdresse.subtract(gesamtflaecheVorher));
    }

    private static LocalDate notAfter(final LocalDate datum, final LocalDate obergrenze) {
        if (datum == null || obergrenze == null) {
            return datum;
        }
        return datum.isAfter(obergrenze) ? obergrenze : datum;
    }

    private static BigDecimal withoutTrailingZeros(final BigDecimal flaeche) {
        final BigDecimal gekuerzt = flaeche.stripTrailingZeros();
        // stripTrailingZeros does not stop at Skalierung 0: it turns 50.00 into 5E+1, which prints so.
        return gekuerzt.scale() < 0 ? gekuerzt.setScale(0) : gekuerzt;
    }

    private static int firstWeek(final LocalDate beginn, final LocalDate beginnWochenlauf) {
        return (int) ChronoUnit.WEEKS.between(beginnWochenlauf, beginn) + 1;
    }

    private static int startedWeeks(final LocalDate beginn, final LocalDate berechnungsEnde) {
        return (int) Math.ceil((double) daysInclusive(beginn, berechnungsEnde) / TAGE_PRO_WOCHE);
    }

    private static int daysInclusive(final LocalDate beginn, final LocalDate berechnungsEnde) {
        return (int) ChronoUnit.DAYS.between(beginn, berechnungsEnde) + 1;
    }

    public int dauerTage() {
        return daysInclusive(beginn, berechnungsEnde);
    }

    public int angefangeneWochen() {
        return startedWeeks(beginn, berechnungsEnde);
    }

    public boolean isInAbrechnungszeitraum() {
        return !beginn.isAfter(berechnungsEnde);
    }

    public boolean hasTakenEffectBy(final int woche) {
        return ersteWoche <= woche && isInAbrechnungszeitraum();
    }

    public boolean isLongerThanOneDay() {
        return berechnungsEnde.isAfter(beginn);
    }
}
