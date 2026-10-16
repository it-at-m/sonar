package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaecheService {

    public static Flaeche from(final AbrechnungPosition position, final int flaechenNummer, final LocalDate berechnungsdatum,
            final LocalDate beginnWochenlauf, final BigDecimal gesamtflaecheVorher) {

        final BigDecimal gesamtflaecheAdresse = withoutTrailingZeros(position.flaeche());

        return new Flaeche(
                "Position " + flaechenNummer,
                position.beginn(),
                notAfter(position.ende(), berechnungsdatum),
                firstWeek(position.beginn(), beginnWochenlauf),
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

    public static boolean hasTakenEffectBy(final Flaeche flaeche, final int woche) {
        return flaeche.ersteWoche() <= woche && flaeche.isInAbrechnungszeitraum();
    }
}
