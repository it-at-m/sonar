package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Überspannungen of one Adresse cost. They are charged Monat by Monat, and the Monate are
 * reported in the Abschnitte a Zwischenabrechnung cuts them into.
 */
public record Ueberspannungsadressabrechnung(
        String adresse,
        String berechnungslog,
        BigDecimal gebuehrUeberspannungen,
        List<BescheiddatenUeberspannung> bescheiddatenUeberspannungen) {

    public Ueberspannungsadressabrechnung {
        bescheiddatenUeberspannungen = bescheiddatenUeberspannungen == null ? List.of() : List.copyOf(bescheiddatenUeberspannungen);
    }
}
