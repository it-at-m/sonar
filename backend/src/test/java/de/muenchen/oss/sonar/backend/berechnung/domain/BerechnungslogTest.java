package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaeche;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Mast;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BerechnungslogTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);

    @Nested
    class Schreibe {

        @Test
        void givenAbrechnung_thenReportEveryGebuehrInItsOwnLine() {
            final Flaeche flaeche = new Flaeche("Position 1", BEGINN, LocalDate.of(2026, 1, 7),
                    1, 1, new BigDecimal("20"), new BigDecimal("20"));
            final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)), 1);
            final Ueberspannungsabrechnung ueberspannungen = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(new Mast(1, BEGINN, LocalDate.of(2026, 3, 31), 1, 3)), 3)));
            final Gebuehren gebuehren = Gebuehren.berechne(
                    flaechen.gebuehr(), ueberspannungen.gebuehr(), new BigDecimal("42"), null);

            final String log = Berechnungslog.schreibe(1, flaechen, ueberspannungen, gebuehren, null);

            assertThat(log).startsWith("Abrechnung 1");
            assertThat(log).contains("====>>>> Die Flächengebühren betragen 30,00€");
            assertThat(log).contains("====>>>> Die Überspannungsgebühren betragen 150,00€");
            assertThat(log).contains("====>>>> Die Verwaltungsgebühren betragen 42,00€");
            assertThat(log).contains("====>>>> Die Gesamtgebühren betragen 222,00€");
        }

        @Test
        void givenAdresseWithMasten_thenGiveItItsOwnBlock() {
            final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(List.of(), 0);
            final Ueberspannungsabrechnung ueberspannungen = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(new Mast(1, BEGINN, LocalDate.of(2026, 3, 31), 1, 3)), 3)));
            final Gebuehren gebuehren = Gebuehren.berechne(
                    flaechen.gebuehr(), ueberspannungen.gebuehr(), new BigDecimal("42"), null);

            final String log = Berechnungslog.schreibe(1, flaechen, ueberspannungen, gebuehren, null);

            assertThat(log).contains("Marienplatz 8");
            assertThat(log).contains("===> Die Gebühr für Adresse Marienplatz 8 beträgt 150,00€");
        }

        @Test
        void givenNoVorabrechnung_thenReportNoZahlbetrag() {
            final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(List.of(), 0);
            final Ueberspannungsabrechnung ueberspannungen = Ueberspannungsabrechnung.berechne(List.of());
            final Gebuehren gebuehren = Gebuehren.berechne(
                    flaechen.gebuehr(), ueberspannungen.gebuehr(), new BigDecimal("42"), null);

            final String log = Berechnungslog.schreibe(1, flaechen, ueberspannungen, gebuehren, null);

            assertThat(log).doesNotContain("Zahlbetrag");
        }

        @Test
        void givenVorabrechnung_thenReportWhatItLeftOver() {
            final Vorabrechnung vorabrechnung = new Vorabrechnung(1, LocalDate.of(2025, 12, 31), new BigDecimal("100.00"));
            final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(List.of(), 0);
            final Ueberspannungsabrechnung ueberspannungen = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(new Mast(1, BEGINN, LocalDate.of(2026, 3, 31), 1, 3)), 3)));
            final Gebuehren gebuehren = Gebuehren.berechne(
                    flaechen.gebuehr(), ueberspannungen.gebuehr(), new BigDecimal("42"), vorabrechnung);

            final String log = Berechnungslog.schreibe(2, flaechen, ueberspannungen, gebuehren, vorabrechnung);

            assertThat(log).startsWith("Abrechnung 2");
            assertThat(log).contains("Zahlbetrag ====>>>> Unter Berücksichtigung von Abrechnung 1 (100,00€) sind zu zahlen: 92,00€");
        }
    }
}
