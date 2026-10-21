package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Überspannungen of a whole Abrechnung cost. Every Adresse counts its Monate from its own
 * first Mast and is charged on its own Masten, so the Abrechnung keeps them apart, and the Bescheid
 * reports them that way too.
 *
 * @param adressen one entry per Adresse that carries at least one Mast
 */
public record Ueberspannungsabrechnung(
        List<Ueberspannungsadressabrechnung> adressen,
        BigDecimal gebuehr) {

    public Ueberspannungsabrechnung {
        adressen = List.copyOf(adressen);
    }
}
