package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record AbrechnungPosition(
        UUID id,
        LocalDate beginn,
        LocalDate ende,
        BigDecimal laenge,
        BigDecimal breite,
        BigDecimal flaeche,
        boolean aufschlag50prozent,
        BigDecimal anteilAnFlaeche) {

    private static final int TAGE_PRO_WOCHE = 7;

    public AbrechnungPosition {
        if (!Zeitraum.isOrdered(beginn, ende)) {
            throw new IllegalArgumentException("ende is before beginn");
        }
    }

    public int getLaufendeWoche(final LocalDate wochenBeginn) {
        // A Position counts its weeks from its own beginn, so a week that already runs when the Position
        // starts is still its first one.
        final LocalDate firstDayOfNutzung = wochenBeginn.isBefore(beginn) ? beginn : wochenBeginn;
        return (int) (ChronoUnit.DAYS.between(beginn, firstDayOfNutzung) / TAGE_PRO_WOCHE) + 1;
    }

    public int getFlaecheQmRoundedUp() {
        // A started square metre is charged as a whole one.
        return flaeche.setScale(0, RoundingMode.CEILING).intValueExact();
    }
}
