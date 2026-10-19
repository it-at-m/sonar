package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Betragsformat;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Ueberspannungstext {

    /** The Zusammenfassung block of one Adresse, with one block per Mast below it. */
    public static String zusammenfassung(final Ueberspannungsadresse adresse) {
        final StringBuilder zusammenfassung = new StringBuilder(adresse.adressbezeichnung()).append(System.lineSeparator())
                .append("Anzahl Monate: ").append(adresse.letzterMonat()).append(System.lineSeparator());
        adresse.masten().forEach(mast -> zusammenfassung.append(zusammenfassung(mast)));
        return zusammenfassung.append(System.lineSeparator()).toString();
    }

    /* default */ static String zusammenfassung(final Mast mast) {
        return "   - Mast " + mast.nummer() + System.lineSeparator()
                + "        Beginn: " + mast.beginn() + "  Ende: " + mast.ende()
                + " (--> " + mast.dauerMonate() + " Monate)" + System.lineSeparator()
                + "        Gültigkeit: Monat " + mast.ersterMonat() + "-" + mast.letzterMonat() + System.lineSeparator();
    }

    /** The block the Berechnungslog of the Abrechnung holds for this Adresse. */
    /* default */ static String protokoll(final Ueberspannungsadresse adresse, final List<Ueberspannungsmonat> monate,
            final BigDecimal gebuehr) {

        final StringBuilder protokoll = new StringBuilder();
        monate.forEach(monat -> protokoll.append(protokoll(monat)));

        return protokoll
                .append("===> Die Gebühr für Adresse ").append(adresse.adressbezeichnung())
                .append(" beträgt ").append(Betragsformat.format(gebuehr)).append('€')
                .append(System.lineSeparator()).append(System.lineSeparator())
                .toString();
    }

    private static String protokoll(final Ueberspannungsmonat monat) {

        final StringBuilder protokoll = new StringBuilder()
                .append("Monat ").append(monat.monat()).append(System.lineSeparator())
                .append("    Anzahl Masten: ").append(monat.anzahlMasten()).append(System.lineSeparator());

        for (final Mastposten posten : monat.posten()) {
            protokoll.append("    Mast ").append(posten.mast().nummer()).append(": ").append(posten.alterInMonaten())
                    .append(". Monat  --> Gebührensatz: ").append(Betragsformat.format(posten.gebuehr())).append('€')
                    .append(System.lineSeparator());
        }

        return protokoll
                .append("    ==> Die Gebühr für Monat ").append(monat.monat()).append(" beträgt ")
                .append(Betragsformat.format(monat.gebuehr())).append('€')
                .append(System.lineSeparator()).append(System.lineSeparator())
                .toString();
    }

    /* default */ static String bescheidzeile(final Ueberspannungsmonat letzterMonat, final int dauerMonate) {
        return "    " + dauerMonate + " Monate x " + Betragsformat.format(letzterMonat.gebuehr())
                + "€ (" + letzterMonat.anzahlMasten() + " Masten)" + System.lineSeparator();
    }
}
