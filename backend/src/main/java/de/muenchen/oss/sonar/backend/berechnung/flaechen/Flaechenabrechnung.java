package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Flächen of a whole Abrechnung cost. They are charged Woche by Woche, and the Wochen are
 * reported in the Abschnitte a Zwischenabrechnung cuts them into.
 */
public record Flaechenabrechnung(
        List<Flaechenwoche> wochen,
        List<BescheiddatenFlaeche> abschnitte,
        BigDecimal gebuehr) {

    public Flaechenabrechnung {
        wochen = List.copyOf(wochen);
        abschnitte = List.copyOf(abschnitte);
    }
}
