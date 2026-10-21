package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenbeitragServiceTest {

    @Nested
    class ZwischenabrechnungGrund {

        @Test
        void givenFlaecheOfASingleTag_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 15), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 3, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).isEmpty();
        }

        @Test
        void givenErsteWocheOfAGrowingFlaeche_thenReportTheFlaechenerweiterung() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 1, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).contains("Flächenerweiterung +100m² (Fläche Position 1)");
        }

        @Test
        void givenErsteWocheOfAShrinkingFlaeche_thenReportTheFlaechenreduzierung() {
            final Flaeche flaeche = new Flaeche("Position 2", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5,
                    new BigDecimal("60"), new BigDecimal("-40"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).contains("Flächenreduzierung -40m² (Fläche Position 2)");
        }

        @Test
        void givenErsteWocheOfTheWochenlaufBeforeTheFlaecheBegins_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), 3,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 1, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).isEmpty();
        }

        @Test
        void givenUnchangedFlaechenzeitindex_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 5, 53);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).isEmpty();
        }

        @Test
        void givenWocheThatMovesTheFlaecheOn_thenReportTheWechsel() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 53);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag))
                    .contains("Wechsel von Zeitindex 1 zu Zeitindex 2 (Fläche Position 1)");
        }

        @Test
        void givenWocheAfterTheLetzteWocheOfTheAdresse_thenReportTheFlaecheAsGone() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, new BigDecimal("100"), 14, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).contains("Fläche nicht mehr aktiv (Fläche Position 1)");
        }

        @Test
        void givenFlaecheThatWasAlreadyReducedAway_thenReturnEmpty() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));

            final Flaechenbeitrag beitrag = new Flaechenbeitrag(flaeche, BigDecimal.ZERO, 5, 13);

            assertThat(FlaechenbeitragService.zwischenabrechnungGrund(beitrag)).isEmpty();
        }
    }
}
