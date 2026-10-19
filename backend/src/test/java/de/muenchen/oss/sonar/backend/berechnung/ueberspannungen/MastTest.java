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
    class IsActive {

        @Test
        void givenMonatBeforeTheMast_thenItIsNotActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isActive(2)).isFalse();
        }

        @Test
        void givenErsterMonatOfTheMast_thenItIsActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isActive(3)).isTrue();
        }

        @Test
        void givenLetzterMonatOfTheMast_thenItIsActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isActive(6)).isTrue();
        }

        @Test
        void givenMonatAfterTheMast_thenItIsNotActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.isActive(7)).isFalse();
        }
    }

    @Nested
    class RunningMonth {

        @Test
        void givenErsterMonatOfTheMast_thenItIsInItsFirstMonat() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);

            assertThat(mast.runningMonth(3)).isEqualTo(1);
            assertThat(mast.runningMonth(6)).isEqualTo(4);
        }
    }
}
