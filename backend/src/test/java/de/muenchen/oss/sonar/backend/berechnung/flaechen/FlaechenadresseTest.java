package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenadresseTest {

    @Nested
    class Beginn {

        @Test
        void givenFlaechen_thenReturnTheEarliestBeginn() {
            final Flaeche fruehere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche spaetere = new Flaeche("Position 2", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3,
                    new BigDecimal("150"), new BigDecimal("50"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(fruehere, spaetere), 5);

            assertThat(adresse.beginn()).contains(LocalDate.of(2026, 1, 1));
        }

        @Test
        void givenNoFlaeche_thenReturnEmpty() {
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(), 0);

            assertThat(adresse.beginn()).isEmpty();
        }
    }

    @Nested
    class Ende {

        @Test
        void givenFlaechen_thenReturnTheLatestBerechnungsEnde() {
            final Flaeche kuerzere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche laengere = new Flaeche("Position 2", LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 31), 2,
                    new BigDecimal("150"), new BigDecimal("50"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(kuerzere, laengere), 5);

            assertThat(adresse.ende()).contains(LocalDate.of(2026, 1, 31));
        }

        @Test
        void givenNoFlaeche_thenReturnEmpty() {
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(), 0);

            assertThat(adresse.ende()).isEmpty();
        }
    }
}
