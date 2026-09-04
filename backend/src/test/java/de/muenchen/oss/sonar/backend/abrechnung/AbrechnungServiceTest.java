package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AbrechnungServiceTest {

    private static final UUID PROJEKT_ID = UUID.randomUUID();
    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "zeitraumVon", "id");

    @Mock
    private AbrechnungRepository abrechnungRepository;

    @Mock
    private ProjektService projektService;

    @Spy
    private final AbrechnungEntityMapper abrechnungEntityMapper = Mappers.getMapper(AbrechnungEntityMapper.class);

    @InjectMocks
    private AbrechnungService unitUnderTest;

    @Nested
    class GetAbrechnungenPage {

        private Sort captureRequestedSort(final AbrechnungSortBy sortBy, final Sort.Direction direction) {
            final ArgumentCaptor<Pageable> pageRequestCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findByProjektId(eq(PROJEKT_ID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, sortBy, direction);

            verify(abrechnungRepository).findByProjektId(eq(PROJEKT_ID), pageRequestCaptor.capture());
            return pageRequestCaptor.getValue().getSort();
        }

        @Test
        void givenPageNumberAndPageSize_thenReturnPageOfAbrechnungen() {
            final Pageable pageRequest = PageRequest.of(0, 10, DEFAULT_SORT);

            final AbrechnungPositionEntity erstePosition = new AbrechnungPositionEntity();
            erstePosition.setBeginn(VON);
            erstePosition.setEnde(BIS);
            erstePosition.setLaenge(new BigDecimal("12.00"));
            erstePosition.setBreite(new BigDecimal("3.00"));
            erstePosition.setFlaeche(new BigDecimal("36.00"));
            erstePosition.setHaelfte(true);
            erstePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity erstesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            erstesNutzungsobjekt.addPosition(erstePosition);

            final AdressdatenEmbeddable ersteAdressdaten = erstesNutzungsobjekt.getAdressdaten();
            ersteAdressdaten.setArt(Adressart.ADRESSE);
            ersteAdressdaten.setAdresse("Marienplatz");
            ersteAdressdaten.setHausnummerVon("8");
            ersteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ersteAbrechnung = new AbrechnungEntity();
            ersteAbrechnung.setProjektId(PROJEKT_ID);
            ersteAbrechnung.setGeschaeftspartnerId("1000000001");
            ersteAbrechnung.setZeitraumVon(VON);
            ersteAbrechnung.setZeitraumBis(BIS);
            ersteAbrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ersteAbrechnung.addNutzungsobjekt(erstesNutzungsobjekt);

            final AbrechnungPositionEntity zweitePosition = new AbrechnungPositionEntity();
            zweitePosition.setBeginn(VON);
            zweitePosition.setEnde(BIS);
            zweitePosition.setLaenge(new BigDecimal("12.00"));
            zweitePosition.setBreite(new BigDecimal("3.00"));
            zweitePosition.setFlaeche(new BigDecimal("36.00"));
            zweitePosition.setHaelfte(true);
            zweitePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity zweitesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            zweitesNutzungsobjekt.addPosition(zweitePosition);

            final AdressdatenEmbeddable zweiteAdressdaten = zweitesNutzungsobjekt.getAdressdaten();
            zweiteAdressdaten.setArt(Adressart.ADRESSE);
            zweiteAdressdaten.setAdresse("Marienplatz");
            zweiteAdressdaten.setHausnummerVon("8");
            zweiteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity zweiteAbrechnung = new AbrechnungEntity();
            zweiteAbrechnung.setProjektId(PROJEKT_ID);
            zweiteAbrechnung.setGeschaeftspartnerId("1000000002");
            zweiteAbrechnung.setZeitraumVon(VON);
            zweiteAbrechnung.setZeitraumBis(BIS);
            zweiteAbrechnung.setAbrechnungsArt(AbrechnungsArt.ZWISCHENABRECHNUNG);
            zweiteAbrechnung.addNutzungsobjekt(zweitesNutzungsobjekt);

            final List<AbrechnungEntity> abrechnungen = List.of(ersteAbrechnung, zweiteAbrechnung);

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findByProjektId(PROJEKT_ID, pageRequest))
                    .thenReturn(new PageImpl<>(abrechnungen, pageRequest, abrechnungen.size()));

            final Page<Abrechnung> result = unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null);

            assertThat(result.getTotalElements()).isEqualTo(abrechnungen.size());
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().getFirst().projektId()).isEqualTo(PROJEKT_ID);
            assertThat(result.getContent().getFirst().abrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
            assertThat(result.getContent().getFirst().nutzungsobjekte()).hasSize(1);
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(PROJEKT_ID.toString());
            verify(abrechnungRepository, never()).findByProjektId(any(UUID.class), any(Pageable.class));
        }

        @Test
        void givenNoSort_thenSortByZeitraumVonDescendingWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(null, null)).isEqualTo(DEFAULT_SORT);
        }

        @Test
        void givenSortByAndDirection_thenSortByThatColumnWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(AbrechnungSortBy.ABRECHNUNGS_ART, Sort.Direction.ASC))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "abrechnungsArt", "id"));
        }

        @Test
        void givenSortByWithoutDirection_thenKeepTheDefaultDirection() {
            assertThat(captureRequestedSort(AbrechnungSortBy.ZEITRAUM_BIS, null))
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "zeitraumBis", "id"));
        }

        @Test
        void givenDirectionWithoutSortBy_thenKeepTheDefaultColumn() {
            assertThat(captureRequestedSort(null, Sort.Direction.ASC))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "zeitraumVon", "id"));
        }
    }

    @Nested
    class CreateAbrechnung {
        @Test
        void givenAbrechnung_thenReturnSavedAbrechnung() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), true, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            final UUID savedId = UUID.randomUUID();
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.save(any(AbrechnungEntity.class))).thenAnswer(invocation -> {
                final AbrechnungEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            final Abrechnung result = unitUnderTest.createAbrechnung(abrechnung);

            verify(abrechnungRepository).save(any(AbrechnungEntity.class));
            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.projektId()).isEqualTo(PROJEKT_ID);
            assertThat(result.nutzungsobjekte()).hasSize(1);

            final AbrechnungNutzungsobjekt savedNutzungsobjekt = result.nutzungsobjekte().getFirst();
            assertThat(savedNutzungsobjekt.adresse()).isEqualTo("Marienplatz");
            assertThat(savedNutzungsobjekt.hausnummerBis()).isEqualTo("12");
            assertThat(savedNutzungsobjekt.tageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(savedNutzungsobjekt.positionen()).hasSize(1);
            assertThat(savedNutzungsobjekt.positionen().getFirst().flaeche()).isEqualByComparingTo("36.00");
            assertThat(savedNutzungsobjekt.positionen().getFirst().haelfte()).isTrue();
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of());
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.createAbrechnung(abrechnung))
                    .isInstanceOf(NotFoundException.class);
            verify(abrechnungRepository, never()).save(any(AbrechnungEntity.class));
        }
    }
}
