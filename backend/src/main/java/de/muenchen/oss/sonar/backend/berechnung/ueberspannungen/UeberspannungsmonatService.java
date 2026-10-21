package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UeberspannungsmonatService {

    public static Ueberspannungsmonat berechne(final Ueberspannungsadresse adresse, final int monat) {

        final List<Mast> oldestFirst = UeberspannungsadresseService.activeMastenOldestFirst(adresse, monat);

        final List<Mastposten> posten = new ArrayList<>(oldestFirst.size());
        for (final Mast mast : oldestFirst) {
            posten.add(new Mastposten(posten.size() + 1, mast, mast.alterInMonaten(monat)));
        }

        return new Ueberspannungsmonat(monat, posten, zwischenabrechnungGrund(adresse, monat, oldestFirst.size()));
    }

    private static String zwischenabrechnungGrund(final Ueberspannungsadresse adresse, final int monat, final int anzahlMasten) {

        final StringBuilder grund = new StringBuilder();

        for (final Mast mast : adresse.masten()) {
            if (mast.isAktivIn(monat)) {
                MastService.zwischenabrechnungGrund(mast, monat)
                        .ifPresent(mastGrund -> grund.append("    ").append(mastGrund).append(System.lineSeparator()));
            }
        }

        if (anzahlMasten != UeberspannungsadresseService.activeMastenOldestFirst(adresse, monat - 1).size()) {
            grund.append("    Anzahl der Masten hat sich geändert auf ").append(anzahlMasten).append(System.lineSeparator());
        }

        return grund.toString();
    }
}
