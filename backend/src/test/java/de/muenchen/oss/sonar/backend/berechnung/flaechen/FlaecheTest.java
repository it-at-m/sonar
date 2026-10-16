package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaecheTest {

    @Nested
    class DauerTage {

        @Test
        void givenZeitraum_thenCountBothBoundaries() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.dauerTage()).isEqualTo(7);
        }
    }

    @Nested
    class AngefangeneWochen {

        @Test
        void givenFullWochen_thenCountThem() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 14), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.angefangeneWochen()).isEqualTo(2);
        }

        @Test
        void givenAngefangeneWoche_thenCountItAsAWholeOne() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.angefangeneWochen()).isEqualTo(3);
        }
    }

    @Nested
    class LetzteWoche {

        @Test
        void givenAngefangeneWoche_thenEndInThatWoche() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.letzteWoche()).isEqualTo(3);
        }

        @Test
        void givenFlaecheStartingInALaterWoche_thenCountFromThere() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.letzteWoche()).isEqualTo(5);
        }
    }

    @Nested
    class IsInAbrechnungszeitraum {

        @Test
        void givenBeginnAfterTheBerechnungsEnde_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.isInAbrechnungszeitraum()).isFalse();
        }

        @Test
        void givenBeginnOnTheBerechnungsEnde_thenReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 15), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.isInAbrechnungszeitraum()).isTrue();
        }
    }

    @Nested
    class IsLongerThanOneDay {

        @Test
        void givenFlaecheOfASingleTag_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 15), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.isLongerThanOneDay()).isFalse();
        }

        @Test
        void givenFlaecheOfTwoTage_thenReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 16), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            assertThat(flaeche.isLongerThanOneDay()).isTrue();
        }
    }
}
