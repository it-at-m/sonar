package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastTest {

    @Nested
    class DauerMonate {

        @Test
        void givenMastOverSeveralMonate_thenCountBothEnds() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.dauerMonate()).isEqualTo(4);
        }

        @Test
        void givenMastEndingWithinItsFirstMonat_thenStillCountThatMonat() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1);

            assertThat(mast.dauerMonate()).isEqualTo(1);
        }
    }

    @Nested
    class LetzterMonat {

        @Test
        void givenMastStartingInTheFirstMonat_thenEndAfterItsDauer() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1);

            assertThat(mast.letzterMonat()).isEqualTo(3);
        }

        @Test
        void givenMastStartingLater_thenCountFromThere() {
            final Mast mast = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.letzterMonat()).isEqualTo(6);
        }
    }

    @Nested
    class IsAktivIn {

        @Test
        void givenMonatBeforeTheMast_thenReturnFalse() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isAktivIn(2)).isFalse();
        }

        @Test
        void givenErsterMonatOfTheMast_thenReturnTrue() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isAktivIn(3)).isTrue();
        }

        @Test
        void givenLetzterMonatOfTheMast_thenReturnTrue() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isAktivIn(6)).isTrue();
        }

        @Test
        void givenMonatAfterTheMast_thenReturnFalse() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isAktivIn(7)).isFalse();
        }
    }

    @Nested
    class AlterInMonaten {

        @Test
        void givenErsterMonatOfTheMast_thenItIsInItsFirstMonat() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.alterInMonaten(3)).isEqualTo(1);
            assertThat(mast.alterInMonaten(6)).isEqualTo(4);
        }
    }
}
