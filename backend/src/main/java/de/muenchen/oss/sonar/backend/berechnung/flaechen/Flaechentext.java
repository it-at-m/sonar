package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Betragsformat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Every line the Flächen contribute to the Zusammenfassung, to the Berechnungslog and to the
 * Bescheid. All of it reaches a reader unchanged, so each text is worded exactly the way it stands
 * here.
 * <p>
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Flaechentext {

    /** The Zusammenfassung block of one Adresse, with one block per Fläche below it. */
    public static String zusammenfassung(final Flaechenadresse adresse) {

        final LocalDate beginn = adresse.beginn().orElse(null);
        final LocalDate ende = adresse.ende().orElse(null);

        final StringBuilder zusammenfassung = new StringBuilder(adresse.adressbezeichnung()).append(System.lineSeparator());

        if (beginn == null || ende.isBefore(beginn)) {
            zusammenfassung.append("   Adresse enthält keine Flächen im Abrechnungszeitraum.").append(System.lineSeparator());
        } else {
            zusammenfassung
                    .append("   50%-Aufschlag : ").append(adresse.isAufschlag50Prozent()).append(System.lineSeparator())
                    .append("   Beginn : ").append(beginn).append(" (Woche : ").append(adresse.flaechen().getFirst().ersteWoche())
                    .append(')')
                    .append(System.lineSeparator())
                    .append("   Ende : ").append(ende).append(" (Woche : ").append(adresse.letzteWoche()).append(')')
                    .append(System.lineSeparator());
            adresse.flaechen().forEach(flaeche -> zusammenfassung.append(zusammenfassung(flaeche)));
        }

        return zusammenfassung.append(System.lineSeparator()).toString();
    }

    /* default */ static String zusammenfassung(final Flaeche flaeche) {

        if (!flaeche.isImAbrechnungszeitraum()) {
            return "   - Fläche " + flaeche.bezeichnung() + System.lineSeparator()
                    + "        Gültigkeit liegt nicht im Abrechnungszeitraum." + System.lineSeparator();
        }

        final String vorzeichen = flaeche.flaechenaenderung().signum() > 0 ? "+" : "";
        return "   - Fläche " + flaeche.bezeichnung() + System.lineSeparator()
                + "        Beginn: " + flaeche.beginn() + "  Ende: " + flaeche.berechnungsEnde()
                + " (" + flaeche.dauerTage() + " Tage --> " + flaeche.angefangeneWochen() + " Wochen)"
                + System.lineSeparator()
                + "        Gesamtfläche Adresse: " + flaeche.gesamtflaecheAdresse() + "m²" + System.lineSeparator()
                + "        Flächenänderung: " + vorzeichen + flaeche.flaechenaenderung() + "m²" + System.lineSeparator()
                + "        Gültigkeit: Woche " + flaeche.ersteWoche() + "-" + flaeche.letzteWoche() + System.lineSeparator();
    }

    public static String protokoll(final Flaechenabrechnung flaechen) {

        final StringBuilder protokoll = new StringBuilder()
                .append("Flächen").append(System.lineSeparator())
                .append("======================").append(System.lineSeparator());
        flaechen.wochen().forEach(woche -> protokoll.append(protokoll(woche)));

        return protokoll
                .append("====>>>> Die Flächengebühren betragen ").append(Betragsformat.format(flaechen.gebuehr())).append('€')
                .append(System.lineSeparator())
                .toString();
    }

    private static String protokoll(final Flaechenwoche woche) {

        final StringBuilder protokoll = new StringBuilder()
                .append("Woche ").append(woche.woche()).append(System.lineSeparator())
                .append(verteilung(woche))
                .append("    Baustellenfläche: ").append(gebuehrenstufe(woche));

        for (final Gebuehrenposten posten : woche.posten()) {
            if (posten.gebuehr().signum() != 0) {
                protokoll.append(protokollzeile(posten));
            }
        }

        return protokoll
                .append("    ==> Die Gebühr für Woche ").append(woche.woche()).append(" beträgt ")
                .append(Betragsformat.format(woche.gebuehr())).append('€')
                .append(System.lineSeparator()).append(System.lineSeparator())
                .toString();
    }

    /* default */ static String verteilung(final Flaechenwoche woche) {
        return woche.verteilung().stream().map(Flaechentext::zeile).reduce("", String::concat);
    }

    private static String zeile(final Flaechenverteilung verteilung) {
        return "    " + verteilung.adressbezeichnung() + ": " + verteilung.flaecheQm() + "m² / "
                + verteilung.zeitindex().getBezeichnung()
                + " (Fläche: " + verteilung.flaechennamen() + ")"
                + (verteilung.isAufschlag50Prozent() ? " (50% Aufschlag)" : "")
                + System.lineSeparator();
    }

    /* default */ static String gebuehrenstufe(final Flaechenwoche woche) {
        return woche.baustellenflaecheQm() + "m² --> " + woche.gebuehrenstufe().getBezeichnung() + System.lineSeparator();
    }

    /* default */ static String gebuehrenstufeOhneWoche() {
        return BigDecimal.ZERO + "m² --> " + System.lineSeparator();
    }

    /* default */ static String bescheidzeile(final Gebuehrenposten posten, final int dauerWochen) {
        return "    " + dauerWochen + " Wochen x " + posten(posten, posten.gebuehrFuer(dauerWochen));
    }

    private static String protokollzeile(final Gebuehrenposten posten) {
        return "    " + posten(posten, posten.gebuehr());
    }

    private static String posten(final Gebuehrenposten posten, final BigDecimal gebuehr) {
        return posten.flaecheGerundetQm() + "m² (" + posten.flaecheQm() + "m²) x "
                + Betragsformat.format(posten.gebuehrensatz()) + "€ ("
                + posten.gebuehrenstufe().getBezeichnung() + " / " + posten.zeitindex().getBezeichnung() + ") = "
                + Betragsformat.format(gebuehr) + "€"
                + (posten.isAufschlag50Prozent() ? " (50%-Aufschlag)" : "")
                + System.lineSeparator();
    }
}
