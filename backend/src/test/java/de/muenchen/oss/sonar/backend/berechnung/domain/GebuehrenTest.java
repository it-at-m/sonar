package de.muenchen.oss.sonar.backend.berechnung.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GebuehrenTest {

    @Nested
    class Berechne {

        @Test
        void givenFlaechenUeberspannungenAndVerwaltung_thenAddThemUp() {
            final Gebuehren gebuehren = Gebuehren.berechne(
                    new BigDecimal("30.00"), new BigDecimal("150.00"), new BigDecimal("42"), null);

            assertThat(gebuehren.gesamt()).isEqualByComparingTo("222.00");
        }

        @Test
        void givenNoVorabrechnung_thenChargeTheWholeGesamtbetrag() {
            final Gebuehren gebuehren = Gebuehren.berechne(
                    new BigDecimal("30.00"), new BigDecimal("150.00"), new BigDecimal("42"), null);

            assertThat(gebuehren.zahlung()).isEqualByComparingTo(gebuehren.gesamt());
        }

        @Test
        void givenVorabrechnung_thenChargeOnlyWhatItLeftOver() {
            final Vorabrechnung vorabrechnung = new Vorabrechnung(1, LocalDate.of(2026, 3, 31), new BigDecimal("100.00"));

            final Gebuehren gebuehren = Gebuehren.berechne(
                    new BigDecimal("30.00"), new BigDecimal("150.00"), new BigDecimal("42"), vorabrechnung);

            assertThat(gebuehren.gesamt()).isEqualByComparingTo("222.00");
            assertThat(gebuehren.zahlung()).isEqualByComparingTo("122.00");
        }
    }
}
