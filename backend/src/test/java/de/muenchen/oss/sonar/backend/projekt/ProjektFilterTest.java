package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjektFilterTest {

    @Nested
    class Projektnummer {
        @Test
        void givenBlankProjektnummer_thenNormalizeToNull() {
            assertThat(new ProjektFilter("   ", null, null, Set.of()).projektnummer()).isNull();
        }

        @Test
        void givenEmptyProjektnummer_thenNormalizeToNull() {
            assertThat(new ProjektFilter("", null, null, Set.of()).projektnummer()).isNull();
        }

        @Test
        void givenPaddedProjektnummer_thenTrimIt() {
            assertThat(new ProjektFilter(" 2026-0001 ", null, null, Set.of()).projektnummer()).isEqualTo("2026-0001");
        }
    }

    @Nested
    class Abrechnungszeitraum {
        @Test
        void givenDates_thenKeepThem() {
            final LocalDate beginn = LocalDate.of(2026, 1, 1);
            final LocalDate ende = LocalDate.of(2026, 3, 31);

            final ProjektFilter result = new ProjektFilter(null, beginn, ende, Set.of());

            assertThat(result.abrechnungBeginn()).isEqualTo(beginn);
            assertThat(result.abrechnungEnde()).isEqualTo(ende);
        }
    }

    @Nested
    class PermittedStadtbezirke {
        @Test
        void givenNullPermittedStadtbezirke_thenThrow() {
            assertThatNullPointerException().isThrownBy(() -> new ProjektFilter(null, null, null, null));
        }

        @Test
        void givenPermittedStadtbezirkeChangedAfterwards_thenKeepTheOriginalOnes() {
            final Set<Stadtbezirk> permittedStadtbezirke = new HashSet<>(Set.of(Stadtbezirk.BEZIRK_06_SENDLING));

            final ProjektFilter result = new ProjektFilter(null, null, null, permittedStadtbezirke);
            permittedStadtbezirke.add(Stadtbezirk.BEZIRK_25_LAIM);

            assertThat(result.permittedStadtbezirke()).containsExactly(Stadtbezirk.BEZIRK_06_SENDLING);
        }
    }
}
