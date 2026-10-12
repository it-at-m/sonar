package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.util.StringUtils;

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

        final LocalDate berechnungsEnde = begrenztesEnde(position, berechnungsdatum);
        final BigDecimal gesamtflaecheAdresse = ohneNachkommanullen(position.flaeche());
        final int ersteWoche = ersteWoche(position.beginn(), beginnWochenlauf);

        return new Flaeche(
                StringUtils.hasText(position.bezeichnung()) ? position.bezeichnung() : "Position " + flaechenNummer,
                position.beginn(),
                berechnungsEnde,
                ersteWoche,
                ersteWoche + angefangeneWochen(position.beginn(), berechnungsEnde) - 1,
                gesamtflaecheAdresse,
                gesamtflaecheAdresse.subtract(gesamtflaecheVorher));
    }

    private static LocalDate begrenztesEnde(final AbrechnungPosition position, final LocalDate berechnungsdatum) {
        final LocalDate ende = position.ende();
        if (ende == null || berechnungsdatum == null) {
            return ende;
        }
        return ende.isAfter(berechnungsdatum) ? berechnungsdatum : ende;
    }

    private static BigDecimal ohneNachkommanullen(final BigDecimal flaeche) {
        final BigDecimal gekuerzt = flaeche.stripTrailingZeros();
        // stripTrailingZeros does not stop at Skalierung 0: it turns 50.00 into 5E+1, which prints so.
        return gekuerzt.scale() < 0 ? gekuerzt.setScale(0) : gekuerzt;
    }

    private static int ersteWoche(final LocalDate beginn, final LocalDate beginnWochenlauf) {
        return (int) ChronoUnit.WEEKS.between(beginnWochenlauf, beginn) + 1;
    }

    private static int angefangeneWochen(final LocalDate beginn, final LocalDate berechnungsEnde) {
        return (int) Math.ceil((double) dauerTage(beginn, berechnungsEnde) / TAGE_PRO_WOCHE);
    }

    private static int dauerTage(final LocalDate beginn, final LocalDate berechnungsEnde) {
        return (int) ChronoUnit.DAYS.between(beginn, berechnungsEnde) + 1;
    }

    public int dauerTage() {
        return dauerTage(beginn, berechnungsEnde);
    }

    public int angefangeneWochen() {
        return angefangeneWochen(beginn, berechnungsEnde);
    }

    public boolean isImAbrechnungszeitraum() {
        return !beginn.isAfter(berechnungsEnde);
    }

    public boolean isWirksamIn(final int woche) {
        return ersteWoche <= woche && isImAbrechnungszeitraum();
    }

    public boolean isLaengerAlsEinTag() {
        return berechnungsEnde.isAfter(beginn);
    }
}
