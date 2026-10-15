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
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), 3, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 2, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenWocheAfterTheLetzteWocheOfTheAdresse_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenFlaecheReducedToNothing_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenFlaecheOutsideTheAbrechnungszeitraum_thenReturnFalse() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15), 5, 2,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 13);

            assertThat(beitrag.isAktiv()).isFalse();
        }

        @Test
        void givenWocheWithinTheGueltigkeit_thenReturnTrue() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 13);

            assertThat(beitrag.isAktiv()).isTrue();
        }
    }

    @Nested
    class Flaechenzeitindex {

        @Test
        void givenErsteWocheOfTheFlaeche_thenReachTheFirstZeitindex() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 12, 31), 3, 55,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 3, 55);

            assertThat(beitrag.flaechenzeitindex()).contains(ZEITINDEX_1);
        }

        @Test
        void givenWocheThatMovesTheFlaecheOn_thenReachTheNextZeitindex() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1, 53,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 53);

            assertThat(beitrag.flaechenzeitindex()).contains(ZEITINDEX_2);
        }

        @Test
        void givenFlaecheThatIsNotAktiv_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.flaechenzeitindex()).isEmpty();
        }
    }

    @Nested
    class ZwischenabrechnungGrund {

        @Test
        void givenFlaecheOfASingleTag_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 15), 3, 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 3, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).isEmpty();
        }

        @Test
        void givenErsteWocheOfAGrowingFlaeche_thenReportTheFlaechenerweiterung() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 1, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).contains("Flächenerweiterung +100m² (Fläche Position 1)");
        }

        @Test
        void givenErsteWocheOfAShrinkingFlaeche_thenReportTheFlaechenreduzierung() {
            final Flaeche flaeche = new Flaeche("Position 2", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5, 13,
                    new BigDecimal("60"), new BigDecimal("-40"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).contains("Flächenreduzierung -40m² (Fläche Position 2)");
        }

        @Test
        void givenErsteWocheOfTheWochenlaufBeforeTheFlaecheBegins_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), 3, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 1, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).isEmpty();
        }

        @Test
        void givenUnchangedFlaechenzeitindex_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1, 53,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 53);

            assertThat(beitrag.zwischenabrechnungGrund()).isEmpty();
        }

        @Test
        void givenWocheThatMovesTheFlaecheOn_thenReportTheWechsel() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1, 53,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 53);

            assertThat(beitrag.zwischenabrechnungGrund())
                    .contains("Wechsel von Zeitindex 1 zu Zeitindex 2 (Fläche Position 1)");
        }

        @Test
        void givenWocheAfterTheLetzteWocheOfTheAdresse_thenReportTheFlaecheAsGone() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).contains("Fläche nicht mehr aktiv (Fläche Position 1)");
        }

        @Test
        void givenFlaecheThatWasAlreadyReducedAway_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(beitrag.zwischenabrechnungGrund()).isEmpty();
        }
    }
}
