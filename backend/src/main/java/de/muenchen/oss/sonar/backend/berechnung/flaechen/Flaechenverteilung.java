package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Zeitindex;
import java.math.BigDecimal;

/**
 * What one Adresse contributes to a Woche under one Zeitindex. The Bescheid lists these so that the
 * Gebühr of the Woche can be followed back to the Adressen it was charged on.
 *
 * @param flaechennamen the Bezeichnungen behind that Fläche, in the order the Flächen begin
 */
public record Flaechenverteilung(
        String adressbezeichnung,
        boolean isAufschlag50Prozent,
        Zeitindex zeitindex,
        BigDecimal flaecheQm,
        String flaechennamen) {

    /* default */ Flaechenverteilung plus(final Flaechenverteilung weitere) {
        return new Flaechenverteilung(adressbezeichnung, isAufschlag50Prozent, zeitindex,
                flaecheQm.add(weitere.flaecheQm),
                flaechennamen + ", " + weitere.flaechennamen);
    }
}
