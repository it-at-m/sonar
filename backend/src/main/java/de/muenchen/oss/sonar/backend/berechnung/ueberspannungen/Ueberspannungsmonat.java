package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * One Monat of the Überspannungsabrechnung of an Adresse, worked out in full.
 */
public record Ueberspannungsmonat(
        int monat,
        List<Mastposten> posten,
        String zwischenabrechnungGrund) {

    public Ueberspannungsmonat {
        posten = List.copyOf(posten);
    }

    public static Ueberspannungsmonat berechne(final Ueberspannungsadresse adresse, final int monat) {

        final List<Mast> oldestFirst = adresse.aktiveMastenOldestFirst(monat);

        final List<Mastposten> posten = new ArrayList<>(oldestFirst.size());
        for (final Mast mast : oldestFirst) {
            posten.add(new Mastposten(posten.size() + 1, mast, mast.alterInMonat(monat)));
        }

        return new Ueberspannungsmonat(monat, posten, zwischenabrechnungGrund(adresse, monat, oldestFirst.size()));
    }

    private static String zwischenabrechnungGrund(final Ueberspannungsadresse adresse, final int monat, final int anzahlMasten) {

        final StringBuilder grund = new StringBuilder();

        for (final Mast mast : adresse.masten()) {
            if (mast.isAktiv(monat)) {
                mast.zwischenabrechnungGrund(monat)
                        .ifPresent(mastGrund -> grund.append("    ").append(mastGrund).append(System.lineSeparator()));
            }
        }

        if (anzahlMasten != adresse.aktiveMastenOldestFirst(monat - 1).size()) {
            grund.append("    Anzahl der Masten hat sich geändert auf ").append(anzahlMasten).append(System.lineSeparator());
        }

        return grund.toString();
    }

    public int anzahlMasten() {
        return posten.size();
    }

    public BigDecimal gebuehr() {
        return posten.stream().map(Mastposten::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
