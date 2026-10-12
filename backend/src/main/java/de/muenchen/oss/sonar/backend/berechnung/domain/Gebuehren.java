package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.math.BigDecimal;

public record Gebuehren(
        BigDecimal flaechen,
        BigDecimal ueberspannungen,
        BigDecimal verwaltung,
        BigDecimal gesamt,
        BigDecimal zahlung) {

    public static Gebuehren berechne(final BigDecimal flaechen, final BigDecimal ueberspannungen, final BigDecimal verwaltung,
            final Vorabrechnung vorabrechnung) {

        final BigDecimal gesamt = ueberspannungen.add(flaechen).add(verwaltung);
        final BigDecimal zahlung = vorabrechnung == null ? gesamt : gesamt.subtract(vorabrechnung.gebuehrGesamt());

        return new Gebuehren(flaechen, ueberspannungen, verwaltung, gesamt, zahlung);
    }
}
