package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public enum Mastzahlstufe {

    ERSTER_MAST(1, "50.00", "50.00"),
    ZWEITER_MAST(2, "0.00", "0.00"),
    WEITERER_MAST(3, "15.00", "22.50");

    private final int abRang;
    private final Map<Mastzeitindex, BigDecimal> saetze;

    Mastzahlstufe(final int abRang, final String... saetzeJeMastzeitindex) {
        this.abRang = abRang;
        this.saetze = new EnumMap<>(Mastzeitindex.class);
        for (final Mastzeitindex mastzeitindex : Mastzeitindex.values()) {
            saetze.put(mastzeitindex, new BigDecimal(saetzeJeMastzeitindex[mastzeitindex.ordinal()]));
        }
    }

    public static Mastzahlstufe from(final int rang) {
        Mastzahlstufe erreicht = ERSTER_MAST;
        for (final Mastzahlstufe stufe : values()) {
            if (rang >= stufe.abRang) {
                erreicht = stufe;
            }
        }
        return erreicht;
    }

    public BigDecimal gebuehrensatzProMonat(final Mastzeitindex mastzeitindex) {
        return saetze.get(mastzeitindex);
    }
}
