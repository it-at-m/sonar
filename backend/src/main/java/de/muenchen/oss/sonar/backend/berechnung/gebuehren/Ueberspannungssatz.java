package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public enum Ueberspannungssatz {

    ERSTER_MAST("50.00", "50.00"),
    ZWEITER_MAST("0.00", "0.00"),
    WEITERER_MAST("15.00", "22.50");

    private final Map<Mastaltersstufe, BigDecimal> saetze;

    Ueberspannungssatz(final String... saetzeJeAltersstufe) {
        this.saetze = new EnumMap<>(Mastaltersstufe.class);
        for (final Mastaltersstufe altersstufe : Mastaltersstufe.values()) {
            saetze.put(altersstufe, new BigDecimal(saetzeJeAltersstufe[altersstufe.ordinal()]));
        }
    }

    public static Ueberspannungssatz from(final int rang) {
        if (rang == 1) {
            return ERSTER_MAST;
        }
        return rang == 2 ? ZWEITER_MAST : WEITERER_MAST;
    }

    public BigDecimal proMonat(final Mastaltersstufe altersstufe) {
        return saetze.get(altersstufe);
    }
}
