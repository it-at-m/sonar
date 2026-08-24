package de.muenchen.oss.sonar.backend.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zeitraum {

    public static boolean isOrdered(final LocalDate von, final LocalDate bis) {
        return von == null || bis == null || !bis.isBefore(von);
    }

    /**
     * A given period wins, because then the days follow from it. Otherwise the directly entered value
     * is kept. Both boundaries count, so a period from the 1st to the 3rd is three days.
     */
    public static Integer tageInklusiv(final LocalDate von, final LocalDate bis, final Integer tage) {
        if (von == null || bis == null) {
            return tage;
        }
        if (bis.isBefore(von)) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.DAYS.between(von, bis) + 1);
    }

}
