package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Mastzahlstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Mastzeitindex;
import java.math.BigDecimal;

public record Mastposten(int rang, Mast mast, int alterInMonaten) {

    public BigDecimal gebuehr() {
        return Mastzahlstufe.from(rang).gebuehrensatzProMonat(Mastzeitindex.from(alterInMonaten));
    }
}
