package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.math.BigDecimal;

public record Gebuehren(
        BigDecimal flaechen,
        BigDecimal ueberspannungen,
        BigDecimal verwaltung,
        BigDecimal gesamt,
        BigDecimal zahlung) {
}
