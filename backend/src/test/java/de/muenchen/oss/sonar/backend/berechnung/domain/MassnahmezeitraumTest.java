package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaeche;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Mast;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MassnahmezeitraumTest {

    @Nested
    class From {

        @Test
        void givenFlaecheBeginningBeforeTheMasten_thenStartOnTheFlaeche() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7),
                    1, 1, new BigDecimal("20"), new BigDecimal("20"));
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), 1, 3);

            final Massnahmezeitraum zeitraum = Massnahmezeitraum.from(
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)),
                    List.of(new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3)));

            assertThat(zeitraum.beginn()).isEqualTo(LocalDate.of(2026, 1, 1));
        }

        @Test
        void givenMastenEndingAfterTheFlaechen_thenEndOnTheMasten() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7),
                    1, 1, new BigDecimal("20"), new BigDecimal("20"));
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), 1, 3);

            final Massnahmezeitraum zeitraum = Massnahmezeitraum.from(
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 1)),
                    List.of(new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3)));

            assertThat(zeitraum.ende()).isEqualTo(LocalDate.of(2026, 5, 31));
        }

        @Test
        void givenSeveralFlaechenadressen_thenRunAsManyWochenAsTheLongestOfThem() {
            final Flaeche kurz = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7),
                    1, 1, new BigDecimal("20"), new BigDecimal("20"));
            final Flaeche lang = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 4),
                    1, 5, new BigDecimal("20"), new BigDecimal("20"));

            final Massnahmezeitraum zeitraum = Massnahmezeitraum.from(
                    List.of(new Flaechenadresse("Marienplatz 8", false, List.of(kurz), 1),
                            new Flaechenadresse("Sendlinger Straße 1", false, List.of(lang), 5)),
                    List.of());

            assertThat(zeitraum.anzahlWochen()).isEqualTo(5);
        }

        @Test
        void givenOnlyUeberspannungen_thenRunNoWocheButKeepTheirZeitraum() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), 1, 3);

            final Massnahmezeitraum zeitraum = Massnahmezeitraum.from(
                    List.of(), List.of(new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 3)));

            assertThat(zeitraum.anzahlWochen()).isZero();
            assertThat(zeitraum.beginn()).isEqualTo(LocalDate.of(2026, 3, 1));
            assertThat(zeitraum.ende()).isEqualTo(LocalDate.of(2026, 5, 31));
        }

        @Test
        void givenNoAdresse_thenFallBackToDatesNoRealZeitraumCanBeat() {
            final Massnahmezeitraum zeitraum = Massnahmezeitraum.from(List.of(), List.of());

            assertThat(zeitraum.beginn()).isEqualTo(Massnahmezeitraum.KEIN_BEGINN);
            assertThat(zeitraum.ende()).isBefore(zeitraum.beginn());
            assertThat(zeitraum.anzahlWochen()).isZero();
        }
    }
}
