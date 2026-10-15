package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechentext;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Betragsformat;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadressabrechnung;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The Berechnungslog of an Abrechnung: every Woche and every Monat that was charged, written out so
 * that a Gebühr can be followed back to what it was charged on.
 * <p>
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Berechnungslog {

    public static String schreibe(final int lfdNr, final Flaechenabrechnung flaechen,
            final Ueberspannungsabrechnung ueberspannungen, final Gebuehren gebuehren, final Vorabrechnung vorabrechnung) {

        final StringBuilder log = new StringBuilder()
                .append("Abrechnung ").append(lfdNr).append(System.lineSeparator())
                .append("~~~~~~~~~~~~").append(System.lineSeparator()).append(System.lineSeparator())
                .append(Flaechentext.protokoll(flaechen))
                .append(System.lineSeparator())
                .append("Überspannungen").append(System.lineSeparator())
                .append("======================").append(System.lineSeparator());

        for (final Ueberspannungsadressabrechnung adresse : ueberspannungen.adressen()) {
            log.append(adresse.adresse()).append(System.lineSeparator())
                    .append("----------------------").append(System.lineSeparator())
                    .append(adresse.berechnungslog());
        }

        log
                .append(System.lineSeparator())
                .append("====>>>> Die Überspannungsgebühren betragen ").append(Betragsformat.format(gebuehren.ueberspannungen())).append('€')
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("====>>>> Die Verwaltungsgebühren betragen ").append(Betragsformat.format(gebuehren.verwaltung())).append('€')
                .append(System.lineSeparator())
                .append("--------------------------------------------------------")
                .append(System.lineSeparator())
                .append("====>>>> Die Gesamtgebühren betragen ").append(Betragsformat.format(gebuehren.gesamt())).append('€')
                .append(System.lineSeparator());

        if (vorabrechnung != null) {
            log
                    .append(System.lineSeparator())
                    .append("Zahlbetrag ====>>>> Unter Berücksichtigung von Abrechnung ").append(vorabrechnung.lfdNr())
                    .append(" (").append(Betragsformat.format(vorabrechnung.gebuehrGesamt())).append("€) sind zu zahlen: ")
                    .append(Betragsformat.format(gebuehren.zahlung())).append('€')
                    .append(System.lineSeparator());
        }

        return log.toString();
    }
}
