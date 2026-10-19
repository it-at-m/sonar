package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;

/**
 * One Monat of the Überspannungsabrechnung of an Adresse, worked out in full.
 */
public record Ueberspannungsmonat(
        int monat,
        List<Mastposten> posten,
        String zwischenabrechnungGrund) {

    public Ueberspannungsmonat {
        posten = List.copyOf(posten);
    }

    public int mastenCount() {
        return posten.size();
    }

    public BigDecimal gebuehr() {
        return posten.stream().map(Mastposten::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
