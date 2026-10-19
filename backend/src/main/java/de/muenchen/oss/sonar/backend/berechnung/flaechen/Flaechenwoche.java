package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechengroessenstufe;
import java.math.BigDecimal;
import java.util.List;

/**
 * One Woche of the Flächenabrechnung, worked out in full. The Flächen are charged Woche by Woche.
 */
public record Flaechenwoche(
        int woche,
        BigDecimal baustellenflaecheQm,
        Flaechengroessenstufe flaechengroessenstufe,
        List<Flaechenverteilung> verteilung,
        List<Gebuehrenposten> posten,
        String zwischenabrechnungGrund) {

    public Flaechenwoche {
        verteilung = List.copyOf(verteilung);
        posten = List.copyOf(posten);
    }

    public BigDecimal gebuehr() {
        return posten.stream().map(Gebuehrenposten::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
