package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenzeitindexTest {

    @Nested
    class From {

        @Test
        void givenFirstWoche_thenStayAtTheFirstZeitindex() {
            assertThat(Flaechenzeitindex.from(0)).isEqualTo(Flaechenzeitindex.ZEITINDEX_1);
        }

        @Test
        void givenWocheBeforeTheSecondStep_thenStayAtTheFirstZeitindex() {
            assertThat(Flaechenzeitindex.from(12)).isEqualTo(Flaechenzeitindex.ZEITINDEX_1);
        }

        @Test
        void givenWocheOfTheSecondStep_thenReachTheSecondZeitindex() {
            assertThat(Flaechenzeitindex.from(13)).isEqualTo(Flaechenzeitindex.ZEITINDEX_2);
        }

        @Test
        void givenWocheBeforeTheThirdStep_thenStayAtTheSecondZeitindex() {
            assertThat(Flaechenzeitindex.from(51)).isEqualTo(Flaechenzeitindex.ZEITINDEX_2);
        }

        @Test
        void givenWocheOfTheThirdStep_thenReachTheThirdZeitindex() {
            assertThat(Flaechenzeitindex.from(52)).isEqualTo(Flaechenzeitindex.ZEITINDEX_3);
        }

        @Test
        void givenWocheBeforeTheFourthStep_thenStayAtTheThirdZeitindex() {
            assertThat(Flaechenzeitindex.from(77)).isEqualTo(Flaechenzeitindex.ZEITINDEX_3);
        }

        @Test
        void givenWocheOfTheFourthStep_thenReachTheFourthZeitindex() {
            assertThat(Flaechenzeitindex.from(78)).isEqualTo(Flaechenzeitindex.ZEITINDEX_4);
        }

        @Test
        void givenWocheFarBeyondTheLastStep_thenStayAtTheFourthZeitindex() {
            assertThat(Flaechenzeitindex.from(1_000)).isEqualTo(Flaechenzeitindex.ZEITINDEX_4);
        }
    }

    @Nested
    class GetBezeichnung {

        @Test
        void givenZeitindex_thenNameItAsTheBescheidPrintsIt() {
            assertThat(Flaechenzeitindex.ZEITINDEX_3.getBezeichnung()).isEqualTo("Zeitindex 3");
        }
    }
}
