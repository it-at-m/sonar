package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;

/**
 * One Abschnitt of the Überspannungen of an Adresse as the Bescheid prints it.
 * <p>
 * This record is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
public record BescheiddatenUeberspannung(
        int monatBeginn,
        int monatEnde,
        int monatDauer,
        String grund,
        String berechnung,
        BigDecimal gebuehr) {
}
