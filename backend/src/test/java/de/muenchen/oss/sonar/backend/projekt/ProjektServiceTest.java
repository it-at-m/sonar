package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektFilter;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektModelMapper;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektSortBy;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
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
    private final ProjektModelMapper projektModelMapper = Mappers.getMapper(ProjektModelMapper.class);

    @InjectMocks
    private ProjektService unitUnderTest;

    private static Projekt projektWithOneAdresse() {
        final ProjektAdresse adresse = new ProjektAdresse();
        adresse.setBezeichnung("Marienplatz 8");
        adresse.setBaunutzung("Gastronomie");
        adresse.setUnerlaubteNutzungVon(BEGINN);
        adresse.setUnerlaubteNutzungBis(ENDE);
        adresse.setAnzahlMahnungen(2);
        adresse.setSondernutzungErlaubt(false);

        final Projekt projekt = new Projekt();
        projekt.setProjektnummer(DEFAULT_PROJEKTNUMMER);
        projekt.setAbrechnungBeginn(BEGINN);
        projekt.setAbrechnungEnde(ENDE);
        projekt.addAdresse(adresse);
        return projekt;
    }

    @Nested
    class GetProjekt {
        @Test
        void givenUUID_thenReturnProjektView() {
            // Given
            final UUID id = UUID.randomUUID();
            final Projekt projekt = projektWithOneAdresse();
            projekt.setId(id);
            when(projektRepository.findById(id)).thenReturn(Optional.of(projekt));

            // When
            final ProjektView result = unitUnderTest.getProjekt(id);

            // Then
            verify(projektRepository).findById(id);
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.abrechnungBeginn()).isEqualTo(BEGINN);
            assertThat(result.abrechnungEnde()).isEqualTo(ENDE);
            assertThat(result.adressen()).hasSize(1);
            assertThat(result.adressen().getFirst().bezeichnung()).isEqualTo("Marienplatz 8");
        }

        @Test
        void givenNonExistentUUID_thenThrowNotFoundException() {
            // Given
            final UUID id = UUID.randomUUID();
            when(projektRepository.findById(id)).thenReturn(Optional.empty());

            // When
            final Exception exception = Assertions.assertThrows(NotFoundException.class, () -> unitUnderTest.getProjekt(id));

            // Then
            verify(projektRepository).findById(id);
            Assertions.assertEquals(String.format("404 NOT_FOUND \"Could not find entity with id %s\"", id), exception.getMessage());
        }
    }

    @Nested
    class GetProjektePage {

        private Sort captureRequestedSort(final ProjektSortBy sortBy, final Sort.Direction direction) {
            final ArgumentCaptor<Pageable> pageRequestCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<Projekt>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            unitUnderTest.getAllProjekte(0, 10, ProjektFilter.none(), sortBy, direction);

            verify(projektRepository).findAll(ArgumentMatchers.<Specification<Projekt>>any(), pageRequestCaptor.capture());
            return pageRequestCaptor.getValue().getSort();
        }

        @Test
        void givenPageNumberAndPageSize_thenReturnPageOfProjektViews() {
            // Given
            final Pageable pageRequest = PageRequest.of(0, 10, DEFAULT_SORT);
            final List<Projekt> projekte = List.of(projektWithOneAdresse(), projektWithOneAdresse());
            when(projektRepository.findAll(ArgumentMatchers.<Specification<Projekt>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(projekte, pageRequest, projekte.size()));

            // When
            final Page<ProjektView> result = unitUnderTest.getAllProjekte(0, 10, ProjektFilter.none(), null, null);

            // Then
            verify(projektRepository).findAll(ArgumentMatchers.<Specification<Projekt>>any(), eq(pageRequest));
            assertThat(result.getTotalElements()).isEqualTo(projekte.size());
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().getFirst().projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.getContent().getFirst().adressen()).hasSize(1);
        }

        @Test
        void givenFilter_thenSearchTheRequestedPage() {
            // Given
            final Pageable pageRequest = PageRequest.of(2, 25, DEFAULT_SORT);
            final ProjektFilter filter = new ProjektFilter("2026-", BEGINN, ENDE);
            when(projektRepository.findAll(ArgumentMatchers.<Specification<Projekt>>any(), eq(pageRequest)))
                    .thenReturn(new PageImpl<>(List.of(projektWithOneAdresse()), pageRequest, 1));

            // When
            final Page<ProjektView> result = unitUnderTest.getAllProjekte(2, 25, filter, null, null);

            // Then
            verify(projektRepository).findAll(ArgumentMatchers.<Specification<Projekt>>any(), eq(pageRequest));
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
        void givenCommand_thenReturnSavedProjektView() {
            // Given
            final CreateProjektCommand.Adresse adresse = new CreateProjektCommand.Adresse(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, true);
            final CreateProjektCommand command = new CreateProjektCommand(DEFAULT_PROJEKTNUMMER, BEGINN, ENDE, List.of(adresse));

            final UUID savedId = UUID.randomUUID();
            when(projektRepository.save(any(Projekt.class))).thenAnswer(invocation -> {
                final Projekt toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            // When
            final ProjektView result = unitUnderTest.createProjekt(command);

            // Then
            verify(projektRepository).save(any(Projekt.class));
            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.adressen()).hasSize(1);
            assertThat(result.adressen().getFirst().bezeichnung()).isEqualTo("Flurstück 1234/5");
            assertThat(result.adressen().getFirst().sondernutzungErlaubt()).isTrue();
        }
    }
}
