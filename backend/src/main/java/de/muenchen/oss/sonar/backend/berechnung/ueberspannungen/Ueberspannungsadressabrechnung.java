package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Überspannungen of one Adresse cost. They are charged Monat by Monat, and the Monate are
 * reported in the Abschnitte a Zwischenabrechnung cuts them into.
 */
public record Ueberspannungsadressabrechnung(
        String adressbezeichnung,
        String berechnungslog,
        BigDecimal gebuehr,
        List<BescheiddatenUeberspannung> abschnitte) {

    public Ueberspannungsadressabrechnung {
        abschnitte = abschnitte == null ? List.of() : List.copyOf(abschnitte);
    }
}
