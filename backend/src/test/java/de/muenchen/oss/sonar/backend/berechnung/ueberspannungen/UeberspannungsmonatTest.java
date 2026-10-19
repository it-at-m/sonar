package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsmonatTest {

    @Nested
    class MastenCount {

        @Test
        void givenPosten_thenCountThem() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(
                    1, List.of(new Mastposten(1, erster, 1), new Mastposten(2, zweiter, 1)), "");

            assertThat(monat.mastenCount()).isEqualTo(2);
        }
    }

    @Nested
    class Gebuehr {

        @Test
        void givenThreeMastenInTheirFirstAltersstufe_thenAddUpTheSaetzeOfTheirRaenge() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(1, List.of(
                    new Mastposten(1, erster, 1), new Mastposten(2, zweiter, 1), new Mastposten(3, dritter, 1)), "");

            assertThat(monat.gebuehr()).isEqualTo(new BigDecimal("65.00"));
        }

        @Test
        void givenThreeMastenInTheirSecondAltersstufe_thenChargeTheHoehererSatzForTheWeitererMast() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1);
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(13, List.of(
                    new Mastposten(1, erster, 13), new Mastposten(2, zweiter, 13), new Mastposten(3, dritter, 13)), "");

            assertThat(monat.gebuehr()).isEqualTo(new BigDecimal("72.50"));
        }

        @Test
        void givenNoMast_thenChargeNothing() {
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(1, List.of(), "");

            assertThat(monat.gebuehr()).isEqualTo(BigDecimal.ZERO);
        }
    }
}
