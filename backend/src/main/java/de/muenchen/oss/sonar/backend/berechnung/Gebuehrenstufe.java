package de.muenchen.oss.sonar.backend.berechnung;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The constants are placeholders.
 */
public final class Gebuehrenstufe {

    private Gebuehrenstufe() {
    }

    public static Stufe getGebuehrenstufe(final int flaechengroesse) {
        if (flaechengroesse > Stufengrenze.STUFENGRENZE_4.getGrenze()) {
            return Stufe.GEBUEHRENSTUFE_5;
        }
        if (flaechengroesse > Stufengrenze.STUFENGRENZE_3.getGrenze()) {
            return Stufe.GEBUEHRENSTUFE_4;
        }
        if (flaechengroesse > Stufengrenze.STUFENGRENZE_2.getGrenze()) {
            return Stufe.GEBUEHRENSTUFE_3;
        }
        if (flaechengroesse > Stufengrenze.STUFENGRENZE_1.getGrenze()) {
            return Stufe.GEBUEHRENSTUFE_2;
        }
        return Stufe.GEBUEHRENSTUFE_1;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Stufengrenze {

        STUFENGRENZE_1(50),

        STUFENGRENZE_2(150),

        STUFENGRENZE_3(300),

        STUFENGRENZE_4(5000);

        private final int grenze;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Stufe {

        GEBUEHRENSTUFE_1("Gebührenstufe 1"),

        GEBUEHRENSTUFE_2("Gebührenstufe 2"),

        GEBUEHRENSTUFE_3("Gebührenstufe 3"),

        GEBUEHRENSTUFE_4("Gebührenstufe 4"),

        GEBUEHRENSTUFE_5("Gebührenstufe 5");

        private final String bezeichnung;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Zeitgrenze {

        ZEITGRENZE_1(13),

        ZEITGRENZE_2(52),

        ZEITGRENZE_3(78);

        private final int grenze;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Zeitindex {

        ZEITINDEX_1("Zeitindex 1"),

        ZEITINDEX_2("Zeitindex 2"),

        ZEITINDEX_3("Zeitindex 3"),

        ZEITINDEX_4("Zeitindex 4");

        private final String bezeichnung;
    }

}
