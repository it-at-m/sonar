package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastzeitindexTest {

    @Nested
    class From {

        @Test
        void givenMastInItsFirstMonat_thenStayAtTheFirstZeitindex() {
            assertThat(Mastzeitindex.from(1)).isEqualTo(Mastzeitindex.ZEITINDEX_1);
        }

        @Test
        void givenMonatBeforeTheFirstOne_thenStayAtTheFirstZeitindex() {
            assertThat(Mastzeitindex.from(0)).isEqualTo(Mastzeitindex.ZEITINDEX_1);
        }

        @Test
        void givenMonatBeforeTheSecondStep_thenStayAtTheFirstZeitindex() {
            assertThat(Mastzeitindex.from(12)).isEqualTo(Mastzeitindex.ZEITINDEX_1);
        }

        @Test
        void givenMonatOfTheSecondStep_thenReachTheSecondZeitindex() {
            assertThat(Mastzeitindex.from(13)).isEqualTo(Mastzeitindex.ZEITINDEX_2);
        }

        @Test
        void givenMonatFarBeyondTheLastStep_thenStayAtTheSecondZeitindex() {
            assertThat(Mastzeitindex.from(100)).isEqualTo(Mastzeitindex.ZEITINDEX_2);
        }
    }

    @Nested
    class GetBezeichnung {

        @Test
        void givenZeitindex_thenNameItAsTheBescheidPrintsIt() {
            assertThat(Mastzeitindex.ZEITINDEX_2.getBezeichnung()).isEqualTo("Altersstufe 2");
        }
    }
}
