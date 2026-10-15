package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * How large the Baustelle is, which is one of the two things the Gebührensatz follows. The Stufe is
 * taken from the whole Baumaßnahme and not from the single Fläche, so every Adresse of one
 * Abrechnung is charged at the same Stufe.
 * <p>
 * The Sätze below are the Gebührenordnung. One line is one Stufe, with its Satz per {@link
 * Flaechenzeitindex} in the order those are declared in.
 * </p>
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public enum Flaechengroessenstufe {

    STUFE_1("Gebührenstufe 1", 50, "1.50", "1.50", "1.50", "1.50"),
    STUFE_2("Gebührenstufe 2", 150, "1.50", "2.00", "2.50", "3.00"),
    STUFE_3("Gebührenstufe 3", 300, "1.50", "2.50", "4.00", "5.00"),
    STUFE_4("Gebührenstufe 4", 500, "1.50", "3.00", "6.00", "8.00"),
    STUFE_5("Gebührenstufe 5", Integer.MAX_VALUE, "1.50", "4.00", "8.50", "12.00");

    private static final BigDecimal AUFSCHLAG_50_PROZENT = new BigDecimal("1.5");

    private static final int NACHKOMMASTELLEN_GEBUEHRENSATZ = 2;

    private final String bezeichnung;
    private final int bisQm;
    private final Map<Flaechenzeitindex, BigDecimal> saetze;

    Flaechengroessenstufe(final String bezeichnung, final int bisQm, final String... saetzeJeFlaechenzeitindex) {
        this.bezeichnung = bezeichnung;
        this.bisQm = bisQm;
        this.saetze = new EnumMap<>(Flaechenzeitindex.class);
        for (final Flaechenzeitindex flaechenzeitindex : Flaechenzeitindex.values()) {
            saetze.put(flaechenzeitindex, new BigDecimal(saetzeJeFlaechenzeitindex[flaechenzeitindex.ordinal()]));
        }
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public static Flaechengroessenstufe from(final int flaecheQm) {
        for (final Flaechengroessenstufe stufe : values()) {
            if (flaecheQm <= stufe.bisQm) {
                return stufe;
            }
        }
        return STUFE_5;
    }

    public BigDecimal gebuehrensatz(final Flaechenzeitindex flaechenzeitindex, final boolean isAufschlag50Prozent) {
        final BigDecimal satz = saetze.get(flaechenzeitindex);
        return isAufschlag50Prozent
                ? satz.multiply(AUFSCHLAG_50_PROZENT).setScale(NACHKOMMASTELLEN_GEBUEHRENSATZ, RoundingMode.HALF_UP)
                : satz;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
