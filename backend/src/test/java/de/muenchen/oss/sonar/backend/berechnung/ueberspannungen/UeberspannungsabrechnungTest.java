package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsabrechnungTest {

    @Nested
    class Berechne {

        @Test
        void givenSeveralAdressen_thenKeepOneAbrechnungPerAdresse() {
            final Mast marienplatz = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 3);
            final Mast sendlinger = new Mast(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31), 1, 2);

            final Ueberspannungsabrechnung abrechnung = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(marienplatz), 3),
                    new Ueberspannungsadresse("Sendlinger Straße 1", List.of(sendlinger), 2)));

            assertThat(abrechnung.adressen()).extracting(Ueberspannungsadressabrechnung::adresse)
                    .containsExactly("Marienplatz 8", "Sendlinger Straße 1");
        }

        @Test
        void givenSeveralAdressen_thenAddTheirGebuehrenUp() {
            final Mast marienplatz = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 3);
            final Mast sendlinger = new Mast(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31), 1, 2);

            final Ueberspannungsabrechnung abrechnung = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(marienplatz), 3),
                    new Ueberspannungsadresse("Sendlinger Straße 1", List.of(sendlinger), 2)));

            assertThat(abrechnung.gebuehr()).isEqualTo(new BigDecimal("250.00"));
        }

        @Test
        void givenAdressenCountingTheirMonateSeparately_thenChargeEachFromItsOwnFirstMast() {
            final Mast frueh = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast spaet = new Mast(1, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 2, 28), 1, 2);

            final Ueberspannungsabrechnung abrechnung = Ueberspannungsabrechnung.berechne(List.of(
                    new Ueberspannungsadresse("Marienplatz 8", List.of(frueh), 14),
                    new Ueberspannungsadresse("Sendlinger Straße 1", List.of(spaet), 2)));

            assertThat(abrechnung.adressen()).extracting(Ueberspannungsadressabrechnung::gebuehrUeberspannungen)
                    .containsExactly(new BigDecimal("700.00"), new BigDecimal("100.00"));
        }

        @Test
        void givenNoAdresse_thenChargeNothing() {
            final Ueberspannungsabrechnung abrechnung = Ueberspannungsabrechnung.berechne(List.of());

            assertThat(abrechnung.adressen()).isEmpty();
            assertThat(abrechnung.gebuehr()).isEqualTo(BigDecimal.ZERO);
        }
    }
}
