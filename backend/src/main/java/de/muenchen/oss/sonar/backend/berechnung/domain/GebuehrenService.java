package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GebuehrenService {

    public static Gebuehren berechne(final BigDecimal flaechen, final BigDecimal ueberspannungen, final BigDecimal verwaltung,
            final Vorabrechnung vorabrechnung) {

        final BigDecimal gesamt = ueberspannungen.add(flaechen).add(verwaltung);
        final BigDecimal zahlung = vorabrechnung == null ? gesamt : gesamt.subtract(vorabrechnung.gebuehrGesamt());

        return new Gebuehren(flaechen, ueberspannungen, verwaltung, gesamt, zahlung);
    }
}
