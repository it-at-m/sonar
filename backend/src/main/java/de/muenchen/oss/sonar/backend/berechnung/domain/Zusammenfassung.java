package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechentext;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungstext;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The Zusammenfassung of an Abrechnung: what was charged, for whom and over which Zeitraum. It
 * reaches the Abrechnung as it stands, so every line is worded exactly this way.
 * <p>
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zusammenfassung {

    public static String schreibe(final int lfdNr, final Projekt projekt, final String username, final Massnahmezeitraum zeitraum,
            final List<Flaechenadresse> eigeneAdressen, final List<Ueberspannungsadresse> adressenMitMasten,
            final List<Flaechenadresse> vorgaengerAdressen) {

        final StringBuilder zusammenfassung = new StringBuilder()
                .append("Abrechnung ").append(lfdNr)
                .append(" von Bauprojekt ").append(projekt.projektnummer()).append(System.lineSeparator());

        if (!projekt.vorgaengerProjekte().isEmpty()) {
            zusammenfassung.append("    Vorgängerprojekte    : ")
                    .append(projekt.vorgaengerProjekte().stream()
                            .map(LinkedProjekt::projektnummer)
                            .collect(Collectors.joining(", ")))
                    .append(System.lineSeparator());
        }

        zusammenfassung
                .append("    Ersteller Abrechnung : ").append(username).append(System.lineSeparator())
                .append("    Anzahl Adressen      : ").append(eigeneAdressen.size()).append(System.lineSeparator())
                .append("    Abrechnungszeitraum  : ").append(zeitraum.beginn())
                .append(" - ").append(zeitraum.ende()).append(System.lineSeparator())
                .append("    Anzahl Wochen : ").append(zeitraum.anzahlWochen()).append(System.lineSeparator())
                .append(System.lineSeparator());

        if (!eigeneAdressen.isEmpty()) {
            zusammenfassung.append("Flächen").append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator());
            eigeneAdressen.forEach(adresse -> zusammenfassung.append(Flaechentext.zusammenfassung(adresse)));
        }

        if (!vorgaengerAdressen.isEmpty()) {
            zusammenfassung.append("Flächen der Vorgängerprojekte").append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator());
            vorgaengerAdressen.forEach(adresse -> zusammenfassung.append(Flaechentext.zusammenfassung(adresse)));
        }

        if (!adressenMitMasten.isEmpty()) {
            zusammenfassung.append("Überspannungen").append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator());
            adressenMitMasten.forEach(adresse -> zusammenfassung.append(Ueberspannungstext.zusammenfassung(adresse)));
        }

        return zusammenfassung.append(System.lineSeparator()).toString();
    }
}
