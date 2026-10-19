package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex.ZEITINDEX_1;
import static de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex.ZEITINDEX_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenbeitragTest {

    @Nested
    class IsAktiv {

        @Test
        void givenWocheBeforeTheErsteWocheOfTheFlaeche_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 2, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenWocheAfterTheLetzteWocheOfTheAdresse_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenFlaecheReducedToNothing_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenFlaecheOutsideTheAbrechnungszeitraum_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenWocheWithinTheGueltigkeit_thenReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 13);

            assertThat(beitrag.isAktiv()).isTrue();
        }
    }

    @Nested
    class Flaechenzeitindex {

        @Test
        void givenErsteWocheOfTheFlaeche_thenReachTheFirstZeitindex() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 12, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 3, 55);

            assertThat(beitrag.flaechenzeitindex()).contains(ZEITINDEX_1);
        }

        @Test
        void givenWocheThatMovesTheFlaecheOn_thenReachTheNextZeitindex() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 53);

            assertThat(beitrag.flaechenzeitindex()).contains(ZEITINDEX_2);
        }

        @Test
        void givenFlaecheThatIsNotAktiv_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.flaechenzeitindex()).isEmpty();
        }
    }
}
