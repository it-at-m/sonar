package de.muenchen.oss.sonar.backend.berechnung;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Gebuehrenstufe {

    private static final BigDecimal AUFSCHLAG_50_PROZENT_FAKTOR = new BigDecimal("1.5");

    private static final int[] GROESSE_NUTZUNG_GRENZEN = { 50, 150, 300, 500 };

    private static final int[] WOCHEN_NUTZUNG_GRENZEN = { 13, 52, 78 };

    private static final BigDecimal[][] GEBUEHR_PRO_QM_TABELLE_STANDARD = {
            { new BigDecimal("1.50"), new BigDecimal("1.50"), new BigDecimal("1.50"), new BigDecimal("1.50"), new BigDecimal("1.50") },
            { new BigDecimal("1.50"), new BigDecimal("2.00"), new BigDecimal("2.50"), new BigDecimal("3.00"), new BigDecimal("4.00") },
            { new BigDecimal("1.50"), new BigDecimal("2.50"), new BigDecimal("4.00"), new BigDecimal("6.00"), new BigDecimal("8.50") },
            { new BigDecimal("1.50"), new BigDecimal("3.00"), new BigDecimal("5.00"), new BigDecimal("8.00"), new BigDecimal("12.00") }
    };

    public static BigDecimal getGebuehrProQm(final int laufendeWoche, final int flaeche, final Boolean aufschlag50prozent) {
        final int wochenindex = getIndex(WOCHEN_NUTZUNG_GRENZEN, laufendeWoche);
        final int groessenindex = getIndex(GROESSE_NUTZUNG_GRENZEN, flaeche);
        final BigDecimal gebuehrProQm = GEBUEHR_PRO_QM_TABELLE_STANDARD[wochenindex][groessenindex];
        if (Boolean.TRUE.equals(aufschlag50prozent)) {
            return gebuehrProQm.multiply(AUFSCHLAG_50_PROZENT_FAKTOR).setScale(2, RoundingMode.HALF_UP);
        }
        return gebuehrProQm;
    }

    private static int getIndex(final int[] grenzen, final int wert) {
        int index = 0;
        while (index < grenzen.length && wert > grenzen[index]) {
            index++;
        }
        return index;
    }

}
