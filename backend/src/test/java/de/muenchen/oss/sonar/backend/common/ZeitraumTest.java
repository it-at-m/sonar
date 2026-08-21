package de.muenchen.oss.sonar.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ZeitraumTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 1, 31);

    @Nested
    class TageInklusiv {
        @Test
        void givenZeitraum_thenCountBothBoundaries() {
            assertThat(Zeitraum.tageInklusiv(VON, BIS, null)).isEqualTo(31);
        }

        @Test
        void givenSingleDay_thenCountOneDay() {
            assertThat(Zeitraum.tageInklusiv(VON, VON, null)).isEqualTo(1);
        }

        @Test
        void givenZeitraumAndTage_thenZeitraumWins() {
            assertThat(Zeitraum.tageInklusiv(VON, BIS, 7)).isEqualTo(31);
        }

        @Test
        void givenInvertedZeitraum_thenReturnNull() {
            assertThat(Zeitraum.tageInklusiv(BIS, VON, null)).isNull();
        }

        @Test
        void givenInvertedZeitraumAndTage_thenReturnNull() {
            assertThat(Zeitraum.tageInklusiv(BIS, VON, 7)).isNull();
        }

        @Test
        void givenOnlyTage_thenKeepThem() {
            assertThat(Zeitraum.tageInklusiv(null, null, 7)).isEqualTo(7);
        }

        @Test
        void givenOnlyVon_thenKeepTheTage() {
            assertThat(Zeitraum.tageInklusiv(VON, null, 7)).isEqualTo(7);
        }

        @Test
        void givenNothing_thenReturnNull() {
            assertThat(Zeitraum.tageInklusiv(null, null, null)).isNull();
        }
    }
}
