package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastpostenTest {

    @Nested
    class Gebuehr {

        @Test
        void givenErsterMastInItsFirstAltersstufe_thenChargeTheVollerSatz() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(new Mastposten(1, mast, 1).gebuehr()).isEqualTo(new BigDecimal("50.00"));
        }

        @Test
        void givenErsterMastInItsSecondAltersstufe_thenChargeTheSameSatz() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(new Mastposten(1, mast, 13).gebuehr()).isEqualTo(new BigDecimal("50.00"));
        }

        @Test
        void givenZweiterMast_thenChargeNothing() {
            final Mast mast = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(new Mastposten(2, mast, 1).gebuehr()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        void givenWeitererMastInItsFirstAltersstufe_thenChargeTheNiedrigererSatz() {
            final Mast mast = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(new Mastposten(3, mast, 12).gebuehr()).isEqualTo(new BigDecimal("15.00"));
        }

        @Test
        void givenWeitererMastInItsSecondAltersstufe_thenChargeTheHoehererSatz() {
            final Mast mast = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(new Mastposten(3, mast, 13).gebuehr()).isEqualTo(new BigDecimal("22.50"));
        }
    }
}
