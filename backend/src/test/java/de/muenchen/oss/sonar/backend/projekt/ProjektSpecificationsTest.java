package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class ProjektSpecificationsTest {

    @Nested
    class ToLikePattern {
        @Test
        void givenTerm_thenMatchAnywhere() {
            assertThat(ProjektSpecifications.toLikePattern("2026-AB")).isEqualTo("%2026-AB%");
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
        void givenNoPermittedStadtbezirke_thenMatchNothing() {
            final Root<ProjektEntity> root = mock();
            final CriteriaQuery<?> query = mock();
            final CriteriaBuilder builder = mock();
            final Predicate nothing = mock();
            when(builder.disjunction()).thenReturn(nothing);

            final Specification<ProjektEntity> result = ProjektSpecifications.matching(new ProjektFilter(null, null, null, Set.of()));

            assertThat(result.toPredicate(root, query, builder)).isSameAs(nothing);
        }

        @Test
        void givenPermittedStadtbezirke_thenMatchOnlyProjekteInThem() {
            final Root<ProjektEntity> root = mock();
            final CriteriaQuery<?> query = mock();
            final CriteriaBuilder builder = mock();
            final Path<Object> stadtbezirk = mock();
            final Predicate inSendlingOrLaim = mock();
            when(root.get("stadtbezirk")).thenReturn(stadtbezirk);
            when(stadtbezirk.in(Set.of(Stadtbezirk.BEZIRK_06_SENDLING, Stadtbezirk.BEZIRK_25_LAIM))).thenReturn(inSendlingOrLaim);

            final Specification<ProjektEntity> result = ProjektSpecifications.matching(
                    new ProjektFilter(null, null, null, Set.of(Stadtbezirk.BEZIRK_06_SENDLING, Stadtbezirk.BEZIRK_25_LAIM)));

            assertThat(result.toPredicate(root, query, builder)).isSameAs(inSendlingOrLaim);
        }

        @Test
        void givenFilledFilter_thenReturnSpecification() {
            final ProjektFilter filter = new ProjektFilter(
                    "2026-", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), Set.of(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL));

            final Specification<ProjektEntity> result = ProjektSpecifications.matching(filter);

            assertThat(result).isNotNull();
        }
    }
}
