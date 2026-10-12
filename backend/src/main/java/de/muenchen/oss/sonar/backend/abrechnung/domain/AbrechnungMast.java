package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.UUID;

public record AbrechnungMast(
        UUID id,
        LocalDate beginn,
        LocalDate ende) {

    public AbrechnungMast {
        if (!Zeitraum.isOrdered(beginn, ende)) {
            throw new IllegalArgumentException("ende is before beginn");
        }
    }
}
