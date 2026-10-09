package de.muenchen.oss.sonar.backend.calculation.flaechen;

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

    public static Flaeche from(final AbrechnungPosition position, final int flaechenNummer, final LocalDate calculationDate,
            final LocalDate beginnWochenlauf, final BigDecimal gesamtflaecheVorher) {

        final LocalDate berechnungsEnde = begrenztesEnde(position, calculationDate);
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

    private static LocalDate begrenztesEnde(final AbrechnungPosition position, final LocalDate calculationDate) {
        final LocalDate ende = position.ende();
        if (ende == null || calculationDate == null) {
            return ende;
        }
        return ende.isAfter(calculationDate) ? calculationDate : ende;
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

    public boolean isImAbrechnungszeitraum() {
        return !beginn.isAfter(berechnungsEnde);
    }

    public boolean isWirksamIn(final int woche) {
        return ersteWoche <= woche && isImAbrechnungszeitraum();
    }

    public boolean isLaengerAlsEinTag() {
        return berechnungsEnde.isAfter(beginn);
    }

    public String zusammenfassung() {

        if (!isImAbrechnungszeitraum()) {
            return "   - Fläche " + bezeichnung + System.lineSeparator()
                    + "        Gültigkeit liegt nicht im Abrechnungszeitraum." + System.lineSeparator();
        }

        final String vorzeichen = flaechenaenderung.signum() > 0 ? "+" : "";
        return "   - Fläche " + bezeichnung + System.lineSeparator()
                + "        Beginn: " + beginn + "  Ende: " + berechnungsEnde
                + " (" + dauerTage(beginn, berechnungsEnde) + " Tage --> " + angefangeneWochen(beginn, berechnungsEnde) + " Wochen)"
                + System.lineSeparator()
                + "        Gesamtfläche Adresse: " + gesamtflaecheAdresse + "m²" + System.lineSeparator()
                + "        Flächenänderung: " + vorzeichen + flaechenaenderung + "m²" + System.lineSeparator()
                + "        Gültigkeit: Woche " + ersteWoche + "-" + letzteWoche + System.lineSeparator();
    }
}
