package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.projekt.model.ProjektFilter;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

/**
 * Covers the parts of the search criteria which do not need a database. Whether the predicates
 * actually select the right rows is covered by {@link ProjektIntegrationTest}.
 */
class ProjektSpecificationsTest {

    @Nested
    class ToLikePattern {
        @Test
        void givenTerm_thenMatchAnywhereInLowercase() {
            assertThat(ProjektSpecifications.toLikePattern("2026-AB")).isEqualTo("%2026-ab%");
        }

        @Test
        void givenPercent_thenEscapeItSoItIsNoWildcard() {
            assertThat(ProjektSpecifications.toLikePattern("50%")).isEqualTo("%50!%%");
        }

        @Test
        void givenUnderscore_thenEscapeItSoItIsNoWildcard() {
            assertThat(ProjektSpecifications.toLikePattern("a_b")).isEqualTo("%a!_b%");
        }

        @Test
        void givenEscapeCharacter_thenEscapeItself() {
            assertThat(ProjektSpecifications.toLikePattern("a!b")).isEqualTo("%a!!b%");
        }
    }

    @Nested
    class Matching {
        @Test
        void givenEmptyFilter_thenReturnUnrestrictedSpecification() {
            final Specification<Projekt> result = ProjektSpecifications.matching(ProjektFilter.none());

            assertThat(result).isNotNull();
        }

        @Test
        void givenFilledFilter_thenReturnSpecification() {
            final ProjektFilter filter = new ProjektFilter(
                    "2026-", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

            final Specification<Projekt> result = ProjektSpecifications.matching(filter);

            assertThat(result).isNotNull();
        }
    }
}
