package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProjektServiceTest {

    private static final String DEFAULT_PROJEKTNUMMER = "2026-0001";
    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "projektnummer", "id");

    @Mock
    private ProjektRepository projektRepository;

    @Spy
    private final ProjektEntityMapper projektEntityMapper = Mappers.getMapper(ProjektEntityMapper.class);

    @Mock
    private VorgaengerService vorgaengerService;

    @InjectMocks
    private ProjektService unitUnderTest;

    @Nested
    class GetProjektePage {

        private Sort captureRequestedSort(final ProjektSortBy sortBy, final Sort.Direction direction) {
            final ArgumentCaptor<Pageable> pageRequestCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            unitUnderTest.getAllProjekte(0, 10, null, null, null, sortBy, direction);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), pageRequestCaptor.capture());
            return pageRequestCaptor.getValue().getSort();
        }

        @Test
        void givenPageNumberAndPageSize_thenReturnPageOfProjekte() {
            final Pageable pageRequest = PageRequest.of(1, 10, DEFAULT_SORT);

            final ProjektEntity erstesProjektEntity = new ProjektEntity();
            erstesProjektEntity.setProjektnummer("2026-0011");
            erstesProjektEntity.setAbrechnungBeginn(BEGINN);
            erstesProjektEntity.setAbrechnungEnde(ENDE);

            final ProjektEntity zweitesProjektEntity = new ProjektEntity();
            zweitesProjektEntity.setProjektnummer("2026-0012");
            zweitesProjektEntity.setAbrechnungBeginn(BEGINN);
            zweitesProjektEntity.setAbrechnungEnde(ENDE);

            final Projekt erstesProjekt = new Projekt(null, "2026-0011", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(),
                    List.of(), false, false);
            final Projekt zweitesProjekt = new Projekt(null, "2026-0012", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(),
                    List.of(), false, false);

            final List<ProjektEntity> projektEntities = List.of(erstesProjektEntity, zweitesProjektEntity);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(projektEntities, pageRequest, 12));
            when(vorgaengerService.toProjekte(projektEntities)).thenReturn(List.of(erstesProjekt, zweitesProjekt));

            final Page<Projekt> result = unitUnderTest.getAllProjekte(1, 10, null, null, null, null, null);

            assertThat(result.getContent()).containsExactly(erstesProjekt, zweitesProjekt);
            assertThat(result.getPageable()).isEqualTo(pageRequest);
            assertThat(result.getTotalElements()).isEqualTo(12);
        }

        @Test
        void givenFilter_thenSearchTheRequestedPage() {
            final Pageable pageRequest = PageRequest.of(2, 25, DEFAULT_SORT);

            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setProjektnummer(DEFAULT_PROJEKTNUMMER);
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);

            final Projekt projekt = new Projekt(null, DEFAULT_PROJEKTNUMMER, BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null, List.of(),
                    List.of(), false, false);

            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(List.of(projektEntity), pageRequest, 51));
            when(vorgaengerService.toProjekte(List.of(projektEntity))).thenReturn(List.of(projekt));

            final Page<Projekt> result = unitUnderTest.getAllProjekte(2, 25, "2026-", BEGINN, ENDE, null, null);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest));
            assertThat(result.getContent()).containsExactly(projekt);
        }

        @Test
        void givenNoSort_thenSortByProjektnummerDescendingWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(null, null)).isEqualTo(DEFAULT_SORT);
        }

        @Test
        void givenSortByAndDirection_thenSortByThatColumnWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(ProjektSortBy.ABRECHNUNG_BEGINN, Sort.Direction.ASC))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "abrechnungBeginn", "id"));
        }

        @Test
        void givenSortByWithoutDirection_thenKeepTheDefaultDirection() {
            assertThat(captureRequestedSort(ProjektSortBy.ABRECHNUNG_ENDE, null))
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "abrechnungEnde", "id"));
        }

        @Test
        void givenDirectionWithoutSortBy_thenKeepTheDefaultColumn() {
            assertThat(captureRequestedSort(null, Sort.Direction.ASC))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "projektnummer", "id"));
        }

        @Test
        void givenAscendingDirection_thenTheTiebreakerAscendsToo() {
            final Sort sort = captureRequestedSort(ProjektSortBy.PROJEKTNUMMER, Sort.Direction.ASC);

            assertThat(sort.getOrderFor("id")).isNotNull();
            assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    class GetProjekt {
        @Test
        void givenKnownId_thenReturnTheProjekt() {
            final UUID projektId = UUID.randomUUID();

            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer(DEFAULT_PROJEKTNUMMER);
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);

            final Projekt projekt = new Projekt(projektId, DEFAULT_PROJEKTNUMMER, BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(), null,
                    List.of(), List.of(), false, false);

            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(vorgaengerService.toProjekt(projektEntity)).thenReturn(projekt);

            final Projekt result = unitUnderTest.getProjekt(projektId);

            assertThat(result).isSameAs(projekt);
        }

        @Test
        void givenUnknownId_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.getProjekt(projektId))
                    .withMessageContaining(projektId.toString());
            verifyNoInteractions(vorgaengerService);
        }
    }
}
