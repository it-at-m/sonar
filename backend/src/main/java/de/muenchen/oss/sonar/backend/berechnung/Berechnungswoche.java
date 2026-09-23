package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Berechnungswoche(int nummer, LocalDate beginn, LocalDate ende) {

    private static final int TAGE_PRO_WOCHE = 7;

    public static List<Berechnungswoche> weeksOf(final LocalDate von, final LocalDate bis) {
        final int tage = Zeitraum.tageInklusiv(von, bis, null);
        final int anzahlWochen = (tage + TAGE_PRO_WOCHE - 1) / TAGE_PRO_WOCHE;

        final List<Berechnungswoche> wochen = new ArrayList<>(anzahlWochen);
        for (int nummer = 1; nummer <= anzahlWochen; nummer++) {
            final LocalDate wochenBeginn = von.plusWeeks(nummer - 1L);
            // The last week runs its full seven days even when the Zeitraum ends inside it, because a
            // started week is charged as a whole one.
            wochen.add(new Berechnungswoche(nummer, wochenBeginn, wochenBeginn.plusDays(TAGE_PRO_WOCHE - 1L)));
        }
        return List.copyOf(wochen);
    }
}
