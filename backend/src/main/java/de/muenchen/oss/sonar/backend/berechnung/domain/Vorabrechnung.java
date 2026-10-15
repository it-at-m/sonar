package de.muenchen.oss.sonar.backend.berechnung.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Vorabrechnung(
        int lfdNr,
        LocalDate abrechnungszeitraumBis,
        BigDecimal gebuehrGesamt) {
}
