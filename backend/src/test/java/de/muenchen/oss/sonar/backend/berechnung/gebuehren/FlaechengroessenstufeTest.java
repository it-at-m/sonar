package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlaechengroessenstufeTest {

    @Nested
    class From {

        @Test
        void givenBaustelleAtTheLimitOfTheFirstStufe_thenStayInTheFirstStufe() {
            assertThat(Flaechengroessenstufe.from(50)).isEqualTo(Flaechengroessenstufe.STUFE_1);
        }

        @Test
        void givenBaustelleBeyondTheFirstStufe_thenReachTheSecondStufe() {
            assertThat(Flaechengroessenstufe.from(51)).isEqualTo(Flaechengroessenstufe.STUFE_2);
        }

        @Test
        void givenBaustelleBeyondTheSecondStufe_thenReachTheThirdStufe() {
            assertThat(Flaechengroessenstufe.from(151)).isEqualTo(Flaechengroessenstufe.STUFE_3);
        }

        @Test
        void givenBaustelleBeyondTheThirdStufe_thenReachTheFourthStufe() {
            assertThat(Flaechengroessenstufe.from(301)).isEqualTo(Flaechengroessenstufe.STUFE_4);
        }

        @Test
        void givenBaustelleBeyondTheFourthStufe_thenReachTheFifthStufe() {
            assertThat(Flaechengroessenstufe.from(501)).isEqualTo(Flaechengroessenstufe.STUFE_5);
        }

        @Test
        void givenBaustelleWithoutFlaeche_thenStayInTheFirstStufe() {
            assertThat(Flaechengroessenstufe.from(0)).isEqualTo(Flaechengroessenstufe.STUFE_1);
        }
    }

    @Nested
    class Gebuehrensatz {

        @ParameterizedTest
        @CsvSource(
            {
                    "STUFE_1, ZEITINDEX_1, 1.50",
                    "STUFE_1, ZEITINDEX_2, 1.50",
                    "STUFE_1, ZEITINDEX_3, 1.50",
                    "STUFE_1, ZEITINDEX_4, 1.50",
                    "STUFE_2, ZEITINDEX_1, 1.50",
                    "STUFE_2, ZEITINDEX_2, 2.00",
                    "STUFE_2, ZEITINDEX_3, 2.50",
                    "STUFE_2, ZEITINDEX_4, 3.00",
                    "STUFE_3, ZEITINDEX_1, 1.50",
                    "STUFE_3, ZEITINDEX_2, 2.50",
                    "STUFE_3, ZEITINDEX_3, 4.00",
                    "STUFE_3, ZEITINDEX_4, 5.00",
                    "STUFE_4, ZEITINDEX_1, 1.50",
                    "STUFE_4, ZEITINDEX_2, 3.00",
                    "STUFE_4, ZEITINDEX_3, 6.00",
                    "STUFE_4, ZEITINDEX_4, 8.00",
                    "STUFE_5, ZEITINDEX_1, 1.50",
                    "STUFE_5, ZEITINDEX_2, 4.00",
                    "STUFE_5, ZEITINDEX_3, 8.50",
                    "STUFE_5, ZEITINDEX_4, 12.00"
            }
        )
        void givenStufeAndZeitindex_thenChargeTheSatzOfTheGebuehrenordnung(final Flaechengroessenstufe stufe,
                final Flaechenzeitindex flaechenzeitindex, final BigDecimal satz) {
            assertThat(stufe.gebuehrensatz(flaechenzeitindex, false)).isEqualTo(satz);
        }

        @Test
        void givenAufschlag_thenChargeHalfAgainOnTopOfTheSatz() {
            assertThat(Flaechengroessenstufe.STUFE_5.gebuehrensatz(Flaechenzeitindex.ZEITINDEX_4, true))
                    .isEqualTo(new BigDecimal("18.00"));
        }

        @Test
        void givenAufschlagOnAHalfCentSatz_thenKeepTwoNachkommastellen() {
            assertThat(Flaechengroessenstufe.STUFE_5.gebuehrensatz(Flaechenzeitindex.ZEITINDEX_3, true))
                    .isEqualTo(new BigDecimal("12.75"));
        }
    }

    @Nested
    class GetBezeichnung {

        @Test
        void givenStufe_thenNameItAsTheBescheidPrintsIt() {
            assertThat(Flaechengroessenstufe.STUFE_4.getBezeichnung()).isEqualTo("Gebührenstufe 4");
        }
    }
}
