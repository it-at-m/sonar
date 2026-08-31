package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.projekt.domain.ProjektAdresse;
import java.time.LocalDate;
import java.util.List;
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

    @InjectMocks
    private ProjektService unitUnderTest;

    @Nested
    class GetProjektePage {

        private Sort captureRequestedSort(final ProjektSortBy sortBy, final Sort.Direction direction) {
            final ArgumentCaptor<Pageable> pageRequestCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            unitUnderTest.getAllProjekte(0, 10, new ProjektFilter(null, null, null), sortBy, direction);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), pageRequestCaptor.capture());
            return pageRequestCaptor.getValue().getSort();
        }

        @Test
        void givenPageNumberAndPageSize_thenReturnPageOfProjekte() {
            final Pageable pageRequest = PageRequest.of(0, 10, DEFAULT_SORT);

            final ProjektAdresseEntity ersteAdresse = new ProjektAdresseEntity();
            ersteAdresse.setBezeichnung("Marienplatz 8");
            ersteAdresse.setBaunutzung("Gastronomie");
            ersteAdresse.setUnerlaubteNutzungVon(BEGINN);
            ersteAdresse.setUnerlaubteNutzungBis(ENDE);
            ersteAdresse.setAnzahlMahnungen(2);
            ersteAdresse.setSondernutzungErlaubt(false);

            final ProjektEntity erstesProjekt = new ProjektEntity();
            erstesProjekt.setProjektnummer(DEFAULT_PROJEKTNUMMER);
            erstesProjekt.setAbrechnungBeginn(BEGINN);
            erstesProjekt.setAbrechnungEnde(ENDE);
            erstesProjekt.addAdresse(ersteAdresse);

            final ProjektAdresseEntity zweiteAdresse = new ProjektAdresseEntity();
            zweiteAdresse.setBezeichnung("Marienplatz 8");
            zweiteAdresse.setBaunutzung("Gastronomie");
            zweiteAdresse.setUnerlaubteNutzungVon(BEGINN);
            zweiteAdresse.setUnerlaubteNutzungBis(ENDE);
            zweiteAdresse.setAnzahlMahnungen(2);
            zweiteAdresse.setSondernutzungErlaubt(false);

            final ProjektEntity zweitesProjekt = new ProjektEntity();
            zweitesProjekt.setProjektnummer(DEFAULT_PROJEKTNUMMER);
            zweitesProjekt.setAbrechnungBeginn(BEGINN);
            zweitesProjekt.setAbrechnungEnde(ENDE);
            zweitesProjekt.addAdresse(zweiteAdresse);

            final List<ProjektEntity> projekte = List.of(erstesProjekt, zweitesProjekt);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(projekte, pageRequest, projekte.size()));

            final Page<Projekt> result = unitUnderTest.getAllProjekte(0, 10, new ProjektFilter(null, null, null), null, null);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest));
            assertThat(result.getTotalElements()).isEqualTo(projekte.size());
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().getFirst().projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.getContent().getFirst().adressen()).hasSize(1);
        }

        @Test
        void givenFilter_thenSearchTheRequestedPage() {
            final Pageable pageRequest = PageRequest.of(2, 25, DEFAULT_SORT);
            final ProjektFilter filter = new ProjektFilter("2026-", BEGINN, ENDE);

            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(BEGINN);
            adresse.setUnerlaubteNutzungBis(ENDE);
            adresse.setAnzahlMahnungen(2);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setProjektnummer(DEFAULT_PROJEKTNUMMER);
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);
            projektEntity.addAdresse(adresse);

            when(projektRepository.findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(List.of(projektEntity), pageRequest, 1));

            final Page<Projekt> result = unitUnderTest.getAllProjekte(2, 25, filter, null, null);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<ProjektEntity>>any(), eq(pageRequest));
            assertThat(result.getContent()).hasSize(1);
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
    class CreateProjekt {
        @Test
        void givenProjekt_thenReturnSavedProjekt() {
            final ProjektAdresse adresse = new ProjektAdresse(
                    null, "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, true);
            final Projekt projekt = new Projekt(null, DEFAULT_PROJEKTNUMMER, BEGINN, ENDE, List.of(adresse));

            final UUID savedId = UUID.randomUUID();
            when(projektRepository.save(any(ProjektEntity.class))).thenAnswer(invocation -> {
                final ProjektEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            final Projekt result = unitUnderTest.createProjekt(projekt);

            verify(projektRepository).save(any(ProjektEntity.class));
            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.adressen()).hasSize(1);
            assertThat(result.adressen().getFirst().bezeichnung()).isEqualTo("Flurstück 1234/5");
            assertThat(result.adressen().getFirst().sondernutzungErlaubt()).isTrue();
        }
    }
}
