package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Writes a Gebühr the way the Bescheid prints it.
 * <p>
 * This class is temporary. It exists for the summary the UI shows, and will be removed.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Betragsformat {

    private static final int NACHKOMMASTELLEN = 2;

    public static String format(final BigDecimal betrag) {
        final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);
        numberFormat.setMinimumFractionDigits(NACHKOMMASTELLEN);
        numberFormat.setMaximumFractionDigits(NACHKOMMASTELLEN);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        return numberFormat.format(betrag);
    }
}
