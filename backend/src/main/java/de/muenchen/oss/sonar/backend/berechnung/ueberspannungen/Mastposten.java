package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Mastaltersstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Ueberspannungssatz;
import java.math.BigDecimal;

public record Mastposten(int rang, Mast mast, int alterInMonaten) {

    public BigDecimal gebuehr() {
        return Ueberspannungssatz.from(rang).proMonat(Mastaltersstufe.from(alterInMonaten));
    }
}
