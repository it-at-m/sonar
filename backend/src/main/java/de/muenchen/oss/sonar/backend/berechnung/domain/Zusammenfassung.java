package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechentext;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungstext;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * The Zusammenfassung of an Abrechnung: what was charged, for whom and over which Zeitraum. It
 * reaches the Abrechnung as it stands, so every line is worded exactly this way.
 * <p>
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zusammenfassung {

    public static String schreibe(final int lfdNr, final Projekt projekt, final String username,
            final LocalDate beginnMassnahme, final LocalDate endeMassnahme, final int anzahlWochen,
            final List<Flaechenadresse> alleAdressen, final List<Ueberspannungsadresse> adressenMitMasten) {

        final StringBuilder zusammenfassung = new StringBuilder()
                .append("Abrechnung ").append(lfdNr)
                .append(" von Bauprojekt ").append(bauprojekt(projekt))
                .append(" (").append(projekt.projektnummer()).append(')').append(System.lineSeparator())
                .append("    Ersteller Abrechnung : ").append(username).append(System.lineSeparator())
                .append("    Anzahl Adressen      : ").append(alleAdressen.size()).append(System.lineSeparator())
                .append("    Abrechnungszeitraum  : ").append(beginnMassnahme)
                .append(" - ").append(endeMassnahme).append(System.lineSeparator())
                .append("    Anzahl Wochen : ").append(anzahlWochen).append(System.lineSeparator())
                .append(System.lineSeparator());

        if (!alleAdressen.isEmpty()) {
            zusammenfassung.append("Flächen").append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator());
            alleAdressen.forEach(adresse -> zusammenfassung.append(Flaechentext.zusammenfassung(adresse)));
        }

        if (!adressenMitMasten.isEmpty()) {
            zusammenfassung.append("Überspannungen").append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator());
            adressenMitMasten.forEach(adresse -> zusammenfassung.append(Ueberspannungstext.zusammenfassung(adresse)));
        }

        return zusammenfassung.append(System.lineSeparator()).toString();
    }

    private static String bauprojekt(final Projekt projekt) {
        return StringUtils.hasText(projekt.projektname()) ? projekt.projektname() : projekt.projektnummer();
    }
}
