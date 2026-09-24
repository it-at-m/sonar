package de.muenchen.oss.sonar.backend.berechnung;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stands in for the Vorgänger that the prototype read from a table of its own. It keeps only the
 * three values the calculation reads, so the ported code needs no change where it uses them.
 */
@Getter
@AllArgsConstructor
public class LetzteAbrechnung {

    private final Integer lfdNr;

    private final LocalDate abrechnungszeitraumBis;

    private final BigDecimal gebuehrGesamt;

}
