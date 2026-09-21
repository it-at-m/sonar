package de.muenchen.oss.sonar.backend.berechnung;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The constants are placeholders.
 */
public final class Gebuehrenstufe {

    private static final Map<Stufe, Map<Zeitindex, BigDecimal>> GEBUEHRENSATZ_TABELLE_STANDARD = Map.of(
            Stufe.GEBUEHRENSTUFE_1, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_2, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_3, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_4, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_5, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")));

    private static final Map<Stufe, Map<Zeitindex, BigDecimal>> GEBUEHRENSATZ_TABELLE_50_PROZENT = Map.of(
            Stufe.GEBUEHRENSTUFE_1, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_2, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_3, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_4, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")),
            Stufe.GEBUEHRENSTUFE_5, Map.of(
                    Zeitindex.ZEITINDEX_1, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_2, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_3, new BigDecimal("3"),
                    Zeitindex.ZEITINDEX_4, new BigDecimal("3")));

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

    public static BigDecimal getGebuehrensatz(final Boolean aufschlag50prozent, final String gebuehrenstufe, final String zeitindex) {
        final Map<Stufe, Map<Zeitindex, BigDecimal>> gebuehrensatzTabelle = Boolean.TRUE.equals(aufschlag50prozent)
                ? GEBUEHRENSATZ_TABELLE_50_PROZENT
                : GEBUEHRENSATZ_TABELLE_STANDARD;
        return gebuehrensatzTabelle.get(Stufe.fromBezeichnung(gebuehrenstufe)).get(Zeitindex.fromBezeichnung(zeitindex));
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

        public static Stufe fromBezeichnung(final String bezeichnung) {
            for (final Stufe stufe : values()) {
                if (stufe.bezeichnung.equals(bezeichnung)) {
                    return stufe;
                }
            }
            throw new IllegalArgumentException(String.format("unknown Gebuehrenstufe %s", bezeichnung));
        }
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

        public static Zeitindex fromBezeichnung(final String bezeichnung) {
            for (final Zeitindex zeitindex : values()) {
                if (zeitindex.bezeichnung.equals(bezeichnung)) {
                    return zeitindex;
                }
            }
            throw new IllegalArgumentException(String.format("unknown Zeitindex %s", bezeichnung));
        }
    }

}
