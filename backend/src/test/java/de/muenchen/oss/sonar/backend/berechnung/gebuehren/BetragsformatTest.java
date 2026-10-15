package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BetragsformatTest {

    @Nested
    class Format {

        @Test
        void givenBetrag_thenSeparateTheNachkommastellenWithAComma() {
            assertThat(Betragsformat.format(new BigDecimal("1.50"))).isEqualTo("1,50");
        }

        @Test
        void givenBetragWithoutNachkommastellen_thenWriteTwoOfThem() {
            assertThat(Betragsformat.format(new BigDecimal("50"))).isEqualTo("50,00");
        }

        @Test
        void givenBetragWithMoreNachkommastellen_thenRoundToTwo() {
            assertThat(Betragsformat.format(new BigDecimal("1.509"))).isEqualTo("1,51");
        }

        @Test
        void givenBetragHalfwayBetweenTwoNachkommastellen_thenRoundUp() {
            assertThat(Betragsformat.format(new BigDecimal("1.005"))).isEqualTo("1,01");
        }

        @Test
        void givenNegativeBetragHalfwayBetweenTwoNachkommastellen_thenRoundAwayFromZero() {
            assertThat(Betragsformat.format(new BigDecimal("-1.005"))).isEqualTo("-1,01");
        }

        @Test
        void givenThousands_thenGroupThemWithADot() {
            assertThat(Betragsformat.format(new BigDecimal("1234567.8"))).isEqualTo("1.234.567,80");
        }

        @Test
        void givenNegativeBetrag_thenKeepTheSign() {
            assertThat(Betragsformat.format(new BigDecimal("-50.00"))).isEqualTo("-50,00");
        }
    }
}
