package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastzahlstufeTest {

    @Nested
    class From {

        @Test
        void givenTheFirstMast_thenChargeItAsErsterMast() {
            assertThat(Mastzahlstufe.from(1)).isEqualTo(Mastzahlstufe.ERSTER_MAST);
        }

        @Test
        void givenTheSecondMast_thenChargeItAsZweiterMast() {
            assertThat(Mastzahlstufe.from(2)).isEqualTo(Mastzahlstufe.ZWEITER_MAST);
        }

        @Test
        void givenTheThirdMast_thenChargeItAsWeitererMast() {
            assertThat(Mastzahlstufe.from(3)).isEqualTo(Mastzahlstufe.WEITERER_MAST);
        }

        @Test
        void givenAHighRang_thenStillChargeItAsWeitererMast() {
            assertThat(Mastzahlstufe.from(17)).isEqualTo(Mastzahlstufe.WEITERER_MAST);
        }
    }

    @Nested
    class GebuehrensatzProMonat {

        @Test
        void givenErsterMast_thenChargeTheSameInBothZeitindizes() {
            assertThat(Mastzahlstufe.ERSTER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_1)).isEqualTo(new BigDecimal("50.00"));
            assertThat(Mastzahlstufe.ERSTER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_2)).isEqualTo(new BigDecimal("50.00"));
        }

        @Test
        void givenZweiterMast_thenChargeNothing() {
            assertThat(Mastzahlstufe.ZWEITER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_1)).isEqualTo(new BigDecimal("0.00"));
            assertThat(Mastzahlstufe.ZWEITER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_2)).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        void givenWeitererMastInTheFirstZeitindex_thenChargeTheLowerSatz() {
            assertThat(Mastzahlstufe.WEITERER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_1)).isEqualTo(new BigDecimal("15.00"));
        }

        @Test
        void givenWeitererMastInTheSecondZeitindex_thenChargeTheHigherSatz() {
            assertThat(Mastzahlstufe.WEITERER_MAST.gebuehrensatzProMonat(Mastzeitindex.ZEITINDEX_2)).isEqualTo(new BigDecimal("22.50"));
        }
    }
}
