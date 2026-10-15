package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import java.math.BigDecimal;

/**
 * One Abschnitt of the Flächen as the Bescheid prints it. Every Text is a block of lines and
 * reaches the Bescheid as it stands.
 * <p>
 * This record is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 *
 * @param grund why the Zwischenabrechnung that opened this Abschnitt was due, and empty for an
 *            Abschnitt that opens nothing
 * @param flaechenVerteilung which Adresse holds how many m² in the last Woche of the Abschnitt
 * @param gebuehrenstufe the Gesamtfläche of that Woche and the Stufe it reaches
 * @param berechnung one line per Gebührenposten of that Woche, charged for every Woche of the
 *            Abschnitt
 */
public record BescheiddatenFlaeche(
        int wocheBeginn,
        int wocheEnde,
        int wocheDauer,
        String flaechenVerteilung,
        String grund,
        String gebuehrenstufe,
        String berechnung,
        BigDecimal gebuehr) {
}
