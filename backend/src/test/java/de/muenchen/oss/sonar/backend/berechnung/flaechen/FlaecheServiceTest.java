package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaecheServiceTest {

    @Nested
    class From {

        @Test
        void givenPosition_thenNameItAfterItsPlaceInTheAdresse() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 2, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.bezeichnung()).isEqualTo("Position 2");
        }

        @Test
        void givenEndeAfterBerechnungsdatum_thenStopAtBerechnungsdatum() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.berechnungsEnde()).isEqualTo(LocalDate.of(2026, 2, 15));
        }

        @Test
        void givenEndeBeforeBerechnungsdatum_thenKeepIt() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.berechnungsEnde()).isEqualTo(LocalDate.of(2026, 1, 20));
        }

        @Test
        void givenNoBerechnungsdatum_thenKeepTheEnde() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, null, LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.berechnungsEnde()).isEqualTo(LocalDate.of(2026, 1, 20));
        }

        @Test
        void givenFlaecheWithNachkommanullen_thenPrintItWithoutThem() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("50.00"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.gesamtflaecheAdresse()).hasToString("50");
        }

        @Test
        void givenFlaecheWithNachkommastellen_thenKeepThem() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("50.50"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.gesamtflaecheAdresse()).hasToString("50.5");
        }

        @Test
        void givenBeginnOnTheFirstTagOfTheWochenlauf_thenStartInWocheOne() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.ersteWoche()).isEqualTo(1);
        }

        @Test
        void givenBeginnOnTheLastTagOfTheFirstWoche_thenStartInWocheOne() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.ersteWoche()).isEqualTo(1);
        }

        @Test
        void givenBeginnInALaterWoche_thenStartInThatWoche() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.ersteWoche()).isEqualTo(2);
        }

        @Test
        void givenAngefangeneWoche_thenEndInThatWoche() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), BigDecimal.ZERO);

            assertThat(flaeche.letzteWoche()).isEqualTo(3);
        }

        @Test
        void givenGroessereFlaecheThanBefore_thenReportTheFlaechenaenderungAsAGain() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("150"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1),
                    new BigDecimal("100"));

            assertThat(flaeche.flaechenaenderung()).hasToString("50");
        }

        @Test
        void givenKleinereFlaecheThanBefore_thenReportTheFlaechenaenderungAsALoss() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("60"), BigDecimal.ZERO);

            final Flaeche flaeche = FlaecheService.berechne(position, 1, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1),
                    new BigDecimal("100"));

            assertThat(flaeche.flaechenaenderung()).hasToString("-40");
        }
    }

    @Nested
    class HasTakenEffectBy {

        @Test
        void givenWocheBeforeTheErsteWoche_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(FlaecheService.hasTakenEffectBy(flaeche, 2)).isFalse();
        }

        @Test
        void givenErsteWoche_thenReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(FlaecheService.hasTakenEffectBy(flaeche, 3)).isTrue();
        }

        @Test
        void givenWocheAfterTheLetzteWoche_thenStillReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(FlaecheService.hasTakenEffectBy(flaeche, 6)).isTrue();
        }

        @Test
        void givenFlaecheOutsideTheAbrechnungszeitraum_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(FlaecheService.hasTakenEffectBy(flaeche, 5)).isFalse();
        }
    }
}
