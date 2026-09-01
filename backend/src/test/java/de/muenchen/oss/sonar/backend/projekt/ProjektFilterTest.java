package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjektFilterTest {

    @Nested
    class Projektnummer {
        @Test
        void givenBlankProjektnummer_thenNormalizeToNull() {
            assertThat(new ProjektFilter("   ", null, null).projektnummer()).isNull();
        }

        @Test
        void givenEmptyProjektnummer_thenNormalizeToNull() {
            assertThat(new ProjektFilter("", null, null).projektnummer()).isNull();
        }

        @Test
        void givenPaddedProjektnummer_thenTrimIt() {
            assertThat(new ProjektFilter(" 2026-0001 ", null, null).projektnummer()).isEqualTo("2026-0001");
        }
    }

    @Nested
    class Abrechnungszeitraum {
        @Test
        void givenDates_thenKeepThem() {
            final LocalDate beginn = LocalDate.of(2026, 1, 1);
            final LocalDate ende = LocalDate.of(2026, 3, 31);

            final ProjektFilter result = new ProjektFilter(null, beginn, ende);

            assertThat(result.abrechnungBeginn()).isEqualTo(beginn);
            assertThat(result.abrechnungEnde()).isEqualTo(ende);
        }
    }
}
