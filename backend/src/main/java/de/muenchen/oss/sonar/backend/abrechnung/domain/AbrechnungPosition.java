package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AbrechnungPosition(
        UUID id,
        LocalDate beginn,
        LocalDate ende,
        BigDecimal laenge,
        BigDecimal breite,
        BigDecimal flaeche,
        boolean haelfte,
        BigDecimal anteilAnFlaeche) {

    public AbrechnungPosition {
        if (!Zeitraum.isOrdered(beginn, ende)) {
            throw new IllegalArgumentException("ende is before beginn");
        }
    }
}
