package de.muenchen.oss.sonar.backend.berechnung.abschnitt;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abschnittsbildung {

    private static final String KEINE_ZWISCHENABRECHNUNG = "";

    public static List<Abschnitt> abschnitte(final List<String> gruende, final boolean grundBeimEinzigenAbschnitt) {

        final List<Abschnitt> abschnitte = new ArrayList<>();

        int von = 1;
        int anzahlZwischenabrechnungen = 0;
        String offenerGrund = KEINE_ZWISCHENABRECHNUNG;

        for (int periode = 1; periode <= gruende.size(); periode++) {

            final String grund = gruende.get(periode - 1);
            if (KEINE_ZWISCHENABRECHNUNG.equals(grund)) {
                continue;
            }

            anzahlZwischenabrechnungen++;

            if (anzahlZwischenabrechnungen > 1) {
                abschnitte.add(new Abschnitt(von, periode - 1, abschnitte.isEmpty() ? KEINE_ZWISCHENABRECHNUNG : offenerGrund));
            }

            von = periode;
            offenerGrund = grund;
        }

        final boolean grundEntfaellt = anzahlZwischenabrechnungen == 1 && !grundBeimEinzigenAbschnitt;
        abschnitte.add(new Abschnitt(von, gruende.size(), grundEntfaellt ? KEINE_ZWISCHENABRECHNUNG : offenerGrund));

        return abschnitte;
    }
}
