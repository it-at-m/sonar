package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.widerspruch.WiderspruchService;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Mock
    private WiderspruchService widerspruchService;

    @Spy
    private final AbrechnungEntityMapper abrechnungEntityMapper = Mappers.getMapper(AbrechnungEntityMapper.class);

    @InjectMocks
    private AbrechnungService unitUnderTest;

    @Nested
    class GetAbrechnungenPage {

        private Sort captureRequestedSort(final List<AbrechnungSortBy> sortBy, final List<Sort.Direction> directions) {
            final ArgumentCaptor<Pageable> pageRequestCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNewestVersionsByProjektId(eq(PROJEKT_ID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, sortBy, directions);

            verify(abrechnungRepository).findNewestVersionsByProjektId(eq(PROJEKT_ID), pageRequestCaptor.capture());
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
            ersteAbrechnung.setId(UUID.randomUUID());
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
            zweiteAbrechnung.setId(UUID.randomUUID());
            zweiteAbrechnung.setProjektId(PROJEKT_ID);
            zweiteAbrechnung.setGeschaeftspartnerId("1000000002");
            zweiteAbrechnung.setZeitraumVon(VON);
            zweiteAbrechnung.setZeitraumBis(BIS);
            zweiteAbrechnung.setAbrechnungsArt(AbrechnungsArt.ZWISCHENABRECHNUNG);
            zweiteAbrechnung.addNutzungsobjekt(zweitesNutzungsobjekt);

            final List<AbrechnungEntity> abrechnungen = List.of(ersteAbrechnung, zweiteAbrechnung);

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNewestVersionsByProjektId(PROJEKT_ID, pageRequest))
                    .thenReturn(new PageImpl<>(abrechnungen, pageRequest, abrechnungen.size()));
            when(widerspruchService.getWiderspruecheOfAbrechnungen(anyCollection())).thenReturn(Map.of());

            final Page<Abrechnung> result = unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null);

            assertThat(result.getTotalElements()).isEqualTo(abrechnungen.size());
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().getFirst().projektId()).isEqualTo(PROJEKT_ID);
            assertThat(result.getContent().getFirst().abrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
            assertThat(result.getContent().getFirst().nutzungsobjekte()).hasSize(1);
        }

        @Test
        void givenOneAbrechnungWithWiderspruch_thenMarkOnlyThatOne() {
            final AbrechnungPositionEntity position = new AbrechnungPositionEntity();
            position.setBeginn(VON);
            position.setEnde(BIS);
            position.setLaenge(new BigDecimal("12.00"));
            position.setBreite(new BigDecimal("3.00"));
            position.setFlaeche(new BigDecimal("36.00"));
            position.setHaelfte(true);
            position.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt.addPosition(position);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final UUID mitWiderspruchId = UUID.randomUUID();
            final AbrechnungEntity mitWiderspruch = new AbrechnungEntity();
            mitWiderspruch.setId(mitWiderspruchId);
            mitWiderspruch.setProjektId(PROJEKT_ID);
            mitWiderspruch.setGeschaeftspartnerId("1000000001");
            mitWiderspruch.setZeitraumVon(VON);
            mitWiderspruch.setZeitraumBis(BIS);
            mitWiderspruch.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            mitWiderspruch.addNutzungsobjekt(nutzungsobjekt);

            final AbrechnungPositionEntity anderePosition = new AbrechnungPositionEntity();
            anderePosition.setBeginn(VON);
            anderePosition.setEnde(BIS);
            anderePosition.setLaenge(new BigDecimal("12.00"));
            anderePosition.setBreite(new BigDecimal("3.00"));
            anderePosition.setFlaeche(new BigDecimal("36.00"));
            anderePosition.setHaelfte(true);
            anderePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity anderesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            anderesNutzungsobjekt.addPosition(anderePosition);

            final AdressdatenEmbeddable andereAdressdaten = anderesNutzungsobjekt.getAdressdaten();
            andereAdressdaten.setArt(Adressart.ADRESSE);
            andereAdressdaten.setAdresse("Sendlinger Straße");
            andereAdressdaten.setHausnummerVon("1");
            andereAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ohneWiderspruch = new AbrechnungEntity();
            ohneWiderspruch.setId(UUID.randomUUID());
            ohneWiderspruch.setProjektId(PROJEKT_ID);
            ohneWiderspruch.setGeschaeftspartnerId("1000000002");
            ohneWiderspruch.setZeitraumVon(VON);
            ohneWiderspruch.setZeitraumBis(BIS);
            ohneWiderspruch.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ohneWiderspruch.addNutzungsobjekt(anderesNutzungsobjekt);

            final Pageable pageRequest = PageRequest.of(0, 10, DEFAULT_SORT);
            final List<AbrechnungEntity> abrechnungen = List.of(mitWiderspruch, ohneWiderspruch);

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNewestVersionsByProjektId(PROJEKT_ID, pageRequest))
                    .thenReturn(new PageImpl<>(abrechnungen, pageRequest, abrechnungen.size()));
            final Widerspruch widerspruch = new Widerspruch(UUID.randomUUID(), mitWiderspruchId,
                    LocalDate.of(2026, 4, 1), null, null, null, null, false, false, null);
            when(widerspruchService.getWiderspruecheOfAbrechnungen(anyCollection()))
                    .thenReturn(Map.of(mitWiderspruchId, widerspruch));

            final Page<Abrechnung> result = unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null);

            assertThat(result.getContent()).extracting(Abrechnung::widerspruchVorhanden).containsExactly(true, false);
        }

        @Test
        void givenAbrechnungCarriedForward_thenReportItsVersionWithoutANewerVersion() {
            final AbrechnungPositionEntity position = new AbrechnungPositionEntity();
            position.setBeginn(VON);
            position.setEnde(BIS);
            position.setLaenge(new BigDecimal("12.00"));
            position.setBreite(new BigDecimal("3.00"));
            position.setFlaeche(new BigDecimal("36.00"));
            position.setHaelfte(true);
            position.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt.addPosition(position);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final UUID vorgaengerId = UUID.randomUUID();
            final AbrechnungEntity neueste = new AbrechnungEntity();
            neueste.setId(UUID.randomUUID());
            neueste.setProjektId(PROJEKT_ID);
            neueste.setVersionsnummer(2);
            neueste.setVorgaengerAbrechnungId(vorgaengerId);
            neueste.setGeschaeftspartnerId("1000000001");
            neueste.setZeitraumVon(VON);
            neueste.setZeitraumBis(BIS);
            neueste.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            neueste.addNutzungsobjekt(nutzungsobjekt);

            final Pageable pageRequest = PageRequest.of(0, 10, DEFAULT_SORT);
            final List<AbrechnungEntity> abrechnungen = List.of(neueste);

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNewestVersionsByProjektId(PROJEKT_ID, pageRequest))
                    .thenReturn(new PageImpl<>(abrechnungen, pageRequest, abrechnungen.size()));
            when(widerspruchService.getWiderspruecheOfAbrechnungen(anyCollection())).thenReturn(Map.of());

            final Page<Abrechnung> result = unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null);

            assertThat(result.getContent().getFirst().versionsnummer()).isEqualTo(2);
            assertThat(result.getContent().getFirst().vorgaengerAbrechnungId()).isEqualTo(vorgaengerId);
            assertThat(result.getContent().getFirst().neuereVersionVorhanden()).isFalse();
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.getAbrechnungenOfProjekt(PROJEKT_ID, 0, 10, null, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(PROJEKT_ID.toString());
            verify(abrechnungRepository, never()).findNewestVersionsByProjektId(any(UUID.class), any(Pageable.class));
        }

        @Test
        void givenNoSort_thenSortByZeitraumVonDescendingWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(null, null)).isEqualTo(DEFAULT_SORT);
        }

        @Test
        void givenEmptySort_thenSortByZeitraumVonDescendingWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(List.of(), List.of())).isEqualTo(DEFAULT_SORT);
        }

        @Test
        void givenSortByAndDirection_thenSortByThatColumnWithIdAsTiebreaker() {
            assertThat(captureRequestedSort(List.of(AbrechnungSortBy.ABRECHNUNGS_ART), List.of(Sort.Direction.ASC)))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "abrechnungsArt", "id"));
        }

        @Test
        void givenSortByWithoutDirection_thenKeepTheDefaultDirection() {
            assertThat(captureRequestedSort(List.of(AbrechnungSortBy.ZEITRAUM_BIS), null))
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "zeitraumBis", "id"));
        }

        @Test
        void givenDirectionWithoutSortBy_thenKeepTheDefaultColumn() {
            assertThat(captureRequestedSort(null, List.of(Sort.Direction.ASC)))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "zeitraumVon", "id"));
        }

        @Test
        void givenSeveralColumns_thenOrderByAllOfThemInTheGivenOrder() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.GESCHAEFTSPARTNER_ID, AbrechnungSortBy.ZEITRAUM_VON);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC, Sort.Direction.DESC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(
                            new Sort.Order(Sort.Direction.ASC, "geschaeftspartnerId"),
                            new Sort.Order(Sort.Direction.DESC, "zeitraumVon"),
                            new Sort.Order(Sort.Direction.DESC, "id")));
        }

        @Test
        void givenFewerDirectionsThanColumns_thenOrderTheRemainingColumnsByTheDefaultDirection() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.ABRECHNUNGS_ART, AbrechnungSortBy.ZEITRAUM_BIS);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(
                            new Sort.Order(Sort.Direction.ASC, "abrechnungsArt"),
                            new Sort.Order(Sort.Direction.DESC, "zeitraumBis"),
                            new Sort.Order(Sort.Direction.DESC, "id")));
        }

        @Test
        void givenBlankColumn_thenIgnoreItTogetherWithItsDirection() {
            final List<AbrechnungSortBy> sortBy = Arrays.asList(AbrechnungSortBy.ABRECHNUNGS_ART, null);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC, Sort.Direction.DESC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "abrechnungsArt", "id"));
        }

        @Test
        void givenOnlyBlankColumns_thenKeepTheDefaultColumn() {
            final List<AbrechnungSortBy> sortBy = Arrays.asList((AbrechnungSortBy) null);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "zeitraumVon", "id"));
        }

        @Test
        void givenBlankDirection_thenKeepTheDefaultDirection() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.ABRECHNUNGS_ART);
            final List<Sort.Direction> directions = Arrays.asList((Sort.Direction) null);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "abrechnungsArt", "id"));
        }

        @Test
        void givenRepeatedColumn_thenOrderByItOnlyAtItsFirstPosition() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.ZEITRAUM_VON, AbrechnungSortBy.ABRECHNUNGS_ART,
                    AbrechnungSortBy.ZEITRAUM_VON);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(
                            new Sort.Order(Sort.Direction.ASC, "zeitraumVon"),
                            new Sort.Order(Sort.Direction.DESC, "abrechnungsArt"),
                            new Sort.Order(Sort.Direction.DESC, "id")));
        }

        @Test
        void givenEveryColumnRepeated_thenOrderByEachOfThemOnce() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.ZEITRAUM_VON, AbrechnungSortBy.ZEITRAUM_BIS,
                    AbrechnungSortBy.ABRECHNUNGS_ART, AbrechnungSortBy.GESCHAEFTSPARTNER_ID, AbrechnungSortBy.ZEITRAUM_VON,
                    AbrechnungSortBy.ZEITRAUM_BIS);

            assertThat(captureRequestedSort(sortBy, null))
                    .isEqualTo(Sort.by(Sort.Direction.DESC, "zeitraumVon", "zeitraumBis", "abrechnungsArt", "geschaeftspartnerId", "id"));
        }

        @Test
        void givenMoreDirectionsThanColumns_thenIgnoreTheSurplus() {
            final List<AbrechnungSortBy> sortBy = List.of(AbrechnungSortBy.ZEITRAUM_BIS);
            final List<Sort.Direction> directions = List.of(Sort.Direction.ASC, Sort.Direction.DESC);

            assertThat(captureRequestedSort(sortBy, directions))
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "zeitraumBis", "id"));
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
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));

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
        void givenAbrechnung_thenSaveItAsTheFirstVersion() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), true, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A,
                    null, null, null, null, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.save(any(AbrechnungEntity.class))).thenAnswer(invocation -> {
                final AbrechnungEntity toSave = invocation.getArgument(0);
                toSave.setId(UUID.randomUUID());
                return toSave;
            });

            final Abrechnung result = unitUnderTest.createAbrechnung(abrechnung);

            final ArgumentCaptor<AbrechnungEntity> savedCaptor = ArgumentCaptor.forClass(AbrechnungEntity.class);
            verify(abrechnungRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().getVersionsnummer()).isEqualTo(1);
            assertThat(savedCaptor.getValue().getVorgaengerAbrechnungId()).isNull();
            assertThat(result.versionsnummer()).isEqualTo(1);
            assertThat(result.neuereVersionVorhanden()).isFalse();
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of());
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.createAbrechnung(abrechnung))
                    .isInstanceOf(NotFoundException.class);
            verify(abrechnungRepository, never()).save(any(AbrechnungEntity.class));
        }
    }

    @Nested
    class GetAbrechnung {
        @Test
        void givenAbrechnungOfProjekt_thenReturnItWithItsVersionsnummer() {
            final AbrechnungPositionEntity position = new AbrechnungPositionEntity();
            position.setBeginn(VON);
            position.setEnde(BIS);
            position.setLaenge(new BigDecimal("12.00"));
            position.setBreite(new BigDecimal("3.00"));
            position.setFlaeche(new BigDecimal("36.00"));
            position.setHaelfte(true);
            position.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt.addPosition(position);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final UUID abrechnungId = UUID.randomUUID();
            final AbrechnungEntity abrechnung = new AbrechnungEntity();
            abrechnung.setId(abrechnungId);
            abrechnung.setProjektId(PROJEKT_ID);
            abrechnung.setVersionsnummer(2);
            abrechnung.setGeschaeftspartnerId("1000000001");
            abrechnung.setZeitraumVon(VON);
            abrechnung.setZeitraumBis(BIS);
            abrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung.addNutzungsobjekt(nutzungsobjekt);

            when(abrechnungRepository.findByIdAndProjektId(abrechnungId, PROJEKT_ID)).thenReturn(Optional.of(abrechnung));
            when(widerspruchService.getWiderspruchOfAbrechnung(abrechnungId)).thenReturn(Optional.empty());
            when(abrechnungRepository.existsByVorgaengerAbrechnungId(abrechnungId)).thenReturn(false);

            final Abrechnung result = unitUnderTest.getAbrechnung(PROJEKT_ID, abrechnungId);

            assertThat(result.id()).isEqualTo(abrechnungId);
            assertThat(result.versionsnummer()).isEqualTo(2);
            assertThat(result.neuereVersionVorhanden()).isFalse();
            assertThat(result.nutzungsobjekte()).hasSize(1);
            assertThat(result.nutzungsobjekte().getFirst().adresse()).isEqualTo("Marienplatz");
        }

        @Test
        void givenAbrechnungWithANewerVersion_thenMarkIt() {
            final AbrechnungPositionEntity position = new AbrechnungPositionEntity();
            position.setBeginn(VON);
            position.setEnde(BIS);
            position.setLaenge(new BigDecimal("12.00"));
            position.setBreite(new BigDecimal("3.00"));
            position.setFlaeche(new BigDecimal("36.00"));
            position.setHaelfte(true);
            position.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt.addPosition(position);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final UUID abrechnungId = UUID.randomUUID();
            final AbrechnungEntity abrechnung = new AbrechnungEntity();
            abrechnung.setId(abrechnungId);
            abrechnung.setProjektId(PROJEKT_ID);
            abrechnung.setVersionsnummer(1);
            abrechnung.setGeschaeftspartnerId("1000000001");
            abrechnung.setZeitraumVon(VON);
            abrechnung.setZeitraumBis(BIS);
            abrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung.addNutzungsobjekt(nutzungsobjekt);

            when(abrechnungRepository.findByIdAndProjektId(abrechnungId, PROJEKT_ID)).thenReturn(Optional.of(abrechnung));
            when(widerspruchService.getWiderspruchOfAbrechnung(abrechnungId)).thenReturn(Optional.empty());
            when(abrechnungRepository.existsByVorgaengerAbrechnungId(abrechnungId)).thenReturn(true);

            assertThat(unitUnderTest.getAbrechnung(PROJEKT_ID, abrechnungId).neuereVersionVorhanden()).isTrue();
        }

        @Test
        void givenAbrechnungOfAnotherProjekt_thenThrowNotFound() {
            final UUID abrechnungId = UUID.randomUUID();
            when(abrechnungRepository.findByIdAndProjektId(abrechnungId, PROJEKT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unitUnderTest.getAbrechnung(PROJEKT_ID, abrechnungId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(abrechnungId.toString());
        }
    }

    @Nested
    class CreateNextVersion {
        @Test
        void givenVorgaenger_thenSaveTheChangedDataAsTheNextVersion() {
            final UUID vorgaengerId = UUID.randomUUID();
            final AbrechnungEntity vorgaenger = new AbrechnungEntity();
            vorgaenger.setId(vorgaengerId);
            vorgaenger.setProjektId(PROJEKT_ID);
            vorgaenger.setVersionsnummer(2);
            vorgaenger.setGeschaeftspartnerId("1000000001");
            vorgaenger.setZeitraumVon(VON);
            vorgaenger.setZeitraumBis(BIS);
            vorgaenger.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("15.00"),
                    new BigDecimal("3.00"), new BigDecimal("45.00"), false, new BigDecimal("45.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, Nutzung.NUTZUNG_A,
                    null, null, null, null, List.of(position));
            final Abrechnung geaenderteAbrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000002", false, null, null,
                    VON, BIS, AbrechnungsArt.ZWISCHENABRECHNUNG, false, false, List.of(nutzungsobjekt));

            final UUID savedId = UUID.randomUUID();
            when(abrechnungRepository.findByIdAndProjektId(vorgaengerId, PROJEKT_ID)).thenReturn(Optional.of(vorgaenger));
            when(abrechnungRepository.existsByVorgaengerAbrechnungId(vorgaengerId)).thenReturn(false);
            when(abrechnungRepository.save(any(AbrechnungEntity.class))).thenAnswer(invocation -> {
                final AbrechnungEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            final Abrechnung result = unitUnderTest.createNextVersion(vorgaengerId, geaenderteAbrechnung);

            final ArgumentCaptor<AbrechnungEntity> savedCaptor = ArgumentCaptor.forClass(AbrechnungEntity.class);
            verify(abrechnungRepository).save(savedCaptor.capture());
            final AbrechnungEntity saved = savedCaptor.getValue();
            assertThat(saved.getVersionsnummer()).isEqualTo(3);
            assertThat(saved.getVorgaengerAbrechnungId()).isEqualTo(vorgaengerId);
            assertThat(saved.getGeschaeftspartnerId()).isEqualTo("1000000002");
            assertThat(saved.getAbrechnungsArt()).isEqualTo(AbrechnungsArt.ZWISCHENABRECHNUNG);
            assertThat(saved.getNutzungsobjekte()).hasSize(1);

            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.versionsnummer()).isEqualTo(3);
            assertThat(result.neuereVersionVorhanden()).isFalse();
            assertThat(result.nutzungsobjekte().getFirst().adresse()).isEqualTo("Sendlinger Straße");
        }

        @Test
        void givenVorgaengerThatAlreadyHasANachfolger_thenThrowConflict() {
            final UUID vorgaengerId = UUID.randomUUID();
            final AbrechnungEntity vorgaenger = new AbrechnungEntity();
            vorgaenger.setId(vorgaengerId);
            vorgaenger.setProjektId(PROJEKT_ID);
            vorgaenger.setVersionsnummer(1);
            vorgaenger.setGeschaeftspartnerId("1000000001");
            vorgaenger.setZeitraumVon(VON);
            vorgaenger.setZeitraumBis(BIS);
            vorgaenger.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of());

            when(abrechnungRepository.findByIdAndProjektId(vorgaengerId, PROJEKT_ID)).thenReturn(Optional.of(vorgaenger));
            when(abrechnungRepository.existsByVorgaengerAbrechnungId(vorgaengerId)).thenReturn(true);

            assertThatThrownBy(() -> unitUnderTest.createNextVersion(vorgaengerId, abrechnung))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(vorgaengerId.toString());
            verify(abrechnungRepository, never()).save(any(AbrechnungEntity.class));
        }

        @Test
        void givenVorgaengerOfAnotherProjekt_thenThrowNotFound() {
            final UUID vorgaengerId = UUID.randomUUID();
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of());
            when(abrechnungRepository.findByIdAndProjektId(vorgaengerId, PROJEKT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unitUnderTest.createNextVersion(vorgaengerId, abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(vorgaengerId.toString());
            verify(abrechnungRepository, never()).save(any(AbrechnungEntity.class));
        }
    }
}
