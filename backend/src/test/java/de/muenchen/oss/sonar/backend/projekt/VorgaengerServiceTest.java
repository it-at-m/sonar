package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.common.BadRequestException;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

@ExtendWith(MockitoExtension.class)
class VorgaengerServiceTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Mock
    private ProjektRepository projektRepository;

    @Spy
    private final ProjektEntityMapper projektEntityMapper = Mappers.getMapper(ProjektEntityMapper.class);

    @InjectMocks
    private VorgaengerService unitUnderTest;

    @Nested
    class ToProjekte {
        @Test
        void givenSeparatelyLinkedProjekte_thenLoadTheLinkedProjekteOneLinkPerQuery() {
            final UUID projekt0001Id = UUID.randomUUID();
            final ProjektEntity projekt0001 = new ProjektEntity();
            projekt0001.setId(projekt0001Id);
            projekt0001.setProjektnummer("2026-0001");

            final UUID projekt0002Id = UUID.randomUUID();
            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setId(projekt0002Id);
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projekt0001Id);

            final UUID projekt0003Id = UUID.randomUUID();
            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setId(projekt0003Id);
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setVorgaengerProjektId(projekt0002Id);

            final UUID projekt0004Id = UUID.randomUUID();
            final ProjektEntity projekt0004 = new ProjektEntity();
            projekt0004.setId(projekt0004Id);
            projekt0004.setProjektnummer("2026-0004");
            projekt0004.setVorgaengerProjektId(projekt0003Id);

            final UUID standaloneId = UUID.randomUUID();
            final ProjektEntity standalone = new ProjektEntity();
            standalone.setId(standaloneId);
            standalone.setProjektnummer("2026-0009");
            standalone.setAbrechnungBeginn(BEGINN);
            standalone.setAbrechnungEnde(ENDE);

            when(projektRepository.findAllById(Set.of(projekt0001Id))).thenReturn(List.of(projekt0001));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projekt0001Id, projekt0002Id, standaloneId)))
                    .thenReturn(List.of(projekt0002, projekt0003));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projekt0003Id))).thenReturn(List.of(projekt0004));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projekt0004Id))).thenReturn(List.of());

            final List<Projekt> result = unitUnderTest.toProjekte(List.of(projekt0002, standalone));

            verify(projektRepository, times(1)).findAllById(any());
            verify(projektRepository, times(3)).findByVorgaengerProjektIdIn(anyCollection());
            assertThat(result.get(0).vorgaengerProjekte()).containsExactly(new LinkedProjekt(projekt0001Id, "2026-0001"));
            assertThat(result.get(0).nachfolgerProjekte()).containsExactly(
                    new LinkedProjekt(projekt0003Id, "2026-0003"),
                    new LinkedProjekt(projekt0004Id, "2026-0004"));
            assertThat(result.get(1).vorgaengerProjekte()).isEmpty();
            assertThat(result.get(1).nachfolgerProjekte()).isEmpty();
        }

        @Test
        void givenNoProjekte_thenLoadNoLinkedProjekte() {
            final List<Projekt> result = unitUnderTest.toProjekte(List.of());

            assertThat(result).isEmpty();
            verifyNoInteractions(projektRepository);
        }

        @Test
        void givenStandaloneProjekte_thenMarkThoseWithAPossibleVorgaengerInOneQuery() {
            final UUID mitVorgaengerVorschlagId = UUID.randomUUID();
            final ProjektEntity mitVorgaengerVorschlag = new ProjektEntity();
            mitVorgaengerVorschlag.setId(mitVorgaengerVorschlagId);
            mitVorgaengerVorschlag.setProjektnummer("2026-0002");
            mitVorgaengerVorschlag.setAbrechnungBeginn(BEGINN);
            mitVorgaengerVorschlag.setAbrechnungEnde(ENDE);

            final UUID ohneVorgaengerVorschlagId = UUID.randomUUID();
            final ProjektEntity ohneVorgaengerVorschlag = new ProjektEntity();
            ohneVorgaengerVorschlag.setId(ohneVorgaengerVorschlagId);
            ohneVorgaengerVorschlag.setProjektnummer("2026-0001");
            ohneVorgaengerVorschlag.setAbrechnungBeginn(BEGINN);
            ohneVorgaengerVorschlag.setAbrechnungEnde(ENDE);

            final ProjektEntity vorschlag = new ProjektEntity();
            vorschlag.setId(UUID.randomUUID());
            vorschlag.setProjektnummer("2025-0001");
            vorschlag.setAbrechnungBeginn(BEGINN);
            vorschlag.setAbrechnungEnde(ENDE);

            when(projektRepository.findVorgaengerVorschlaege(Set.of(mitVorgaengerVorschlagId, ohneVorgaengerVorschlagId)))
                    .thenReturn(List.of(new VorgaengerVorschlag(mitVorgaengerVorschlagId, vorschlag)));

            final List<Projekt> result = unitUnderTest.toProjekte(List.of(mitVorgaengerVorschlag, ohneVorgaengerVorschlag));

            verify(projektRepository, times(1)).findVorgaengerVorschlaege(anyCollection());
            assertThat(result.get(0).verknuepfungMoeglich()).isTrue();
            assertThat(result.get(1).verknuepfungMoeglich()).isFalse();
        }

        @Test
        void givenOnlyLinkedProjekte_thenLookForNoPossibleVorgaenger() {
            final UUID vorgaengerId = UUID.randomUUID();
            final ProjektEntity vorgaenger = new ProjektEntity();
            vorgaenger.setId(vorgaengerId);
            vorgaenger.setProjektnummer("2026-0001");
            vorgaenger.setAbrechnungBeginn(BEGINN);
            vorgaenger.setAbrechnungEnde(ENDE);

            final ProjektEntity nachfolger = new ProjektEntity();
            nachfolger.setId(UUID.randomUUID());
            nachfolger.setProjektnummer("2026-0002");
            nachfolger.setAbrechnungBeginn(BEGINN);
            nachfolger.setAbrechnungEnde(ENDE);
            nachfolger.setVorgaengerProjektId(vorgaengerId);

            final List<Projekt> result = unitUnderTest.toProjekte(List.of(nachfolger, vorgaenger));

            verify(projektRepository, never()).findVorgaengerVorschlaege(anyCollection());
            assertThat(result).noneMatch(Projekt::verknuepfungMoeglich);
        }

        @Test
        void givenProjektWithVorgaengerAndNachfolger_thenReturnThemInTheirOrder() {
            final UUID projekt0001Id = UUID.randomUUID();
            final ProjektEntity projekt0001 = new ProjektEntity();
            projekt0001.setId(projekt0001Id);
            projekt0001.setProjektnummer("2026-0001");

            final UUID projekt0002Id = UUID.randomUUID();
            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setId(projekt0002Id);
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setVorgaengerProjektId(projekt0001Id);

            final UUID projekt0003Id = UUID.randomUUID();
            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setId(projekt0003Id);
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.setVorgaengerProjektId(projekt0002Id);

            final UUID projekt0004Id = UUID.randomUUID();
            final ProjektEntity projekt0004 = new ProjektEntity();
            projekt0004.setId(projekt0004Id);
            projekt0004.setProjektnummer("2026-0004");
            projekt0004.setVorgaengerProjektId(projekt0003Id);

            when(projektRepository.findAllById(Set.of(projekt0002Id))).thenReturn(List.of(projekt0002));
            when(projektRepository.findAllById(Set.of(projekt0001Id))).thenReturn(List.of(projekt0001));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projekt0001Id, projekt0002Id, projekt0003Id)))
                    .thenReturn(List.of(projekt0002, projekt0003, projekt0004));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projekt0004Id))).thenReturn(List.of());

            final Projekt result = unitUnderTest.toProjekt(projekt0003);

            assertThat(result.vorgaengerProjektId()).isEqualTo(projekt0002Id);
            assertThat(result.vorgaengerProjekte()).containsExactly(
                    new LinkedProjekt(projekt0001Id, "2026-0001"),
                    new LinkedProjekt(projekt0002Id, "2026-0002"));
            assertThat(result.nachfolgerProjekte()).containsExactly(new LinkedProjekt(projekt0004Id, "2026-0004"));
        }

        @Test
        void givenStandaloneProjekt_thenReturnNoLinkedProjekte() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0001");
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);

            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(projektId))).thenReturn(List.of());

            final Projekt result = unitUnderTest.toProjekt(projektEntity);

            assertThat(result.vorgaengerProjektId()).isNull();
            assertThat(result.vorgaengerProjekte()).isEmpty();
            assertThat(result.nachfolgerProjekte()).isEmpty();
            verify(projektRepository, never()).findAllById(any());
        }

        @Test
        void givenLinksClosedIntoACircleInTheDatabase_thenStopAtTheProjektItself() {
            final UUID ersterId = UUID.randomUUID();
            final UUID zweiterId = UUID.randomUUID();

            final ProjektEntity erster = new ProjektEntity();
            erster.setId(ersterId);
            erster.setProjektnummer("2026-0001");
            erster.setAbrechnungBeginn(BEGINN);
            erster.setAbrechnungEnde(ENDE);
            erster.setVorgaengerProjektId(zweiterId);

            final ProjektEntity zweiter = new ProjektEntity();
            zweiter.setId(zweiterId);
            zweiter.setProjektnummer("2026-0002");
            zweiter.setVorgaengerProjektId(ersterId);

            when(projektRepository.findAllById(Set.of(zweiterId))).thenReturn(List.of(zweiter));
            when(projektRepository.findByVorgaengerProjektIdIn(Set.of(ersterId, zweiterId))).thenReturn(List.of(erster, zweiter));

            final Projekt result = unitUnderTest.toProjekt(erster);

            assertThat(result.vorgaengerProjekte()).containsExactly(new LinkedProjekt(zweiterId, "2026-0002"));
            assertThat(result.nachfolgerProjekte()).isEmpty();
        }
    }

    @Nested
    class GetVorgaengerVorschlaege {
        @Test
        void givenVorschlaege_thenKeepTheOrderOfTheQuery() {
            final UUID projektId = UUID.randomUUID();

            final UUID vorschlag0003Id = UUID.randomUUID();
            final ProjektEntity vorschlag0003 = new ProjektEntity();
            vorschlag0003.setId(vorschlag0003Id);
            vorschlag0003.setProjektnummer("2026-0003");
            vorschlag0003.setAbrechnungBeginn(BEGINN);
            vorschlag0003.setAbrechnungEnde(ENDE);

            final UUID vorschlag0002Id = UUID.randomUUID();
            final ProjektEntity vorschlag0002 = new ProjektEntity();
            vorschlag0002.setId(vorschlag0002Id);
            vorschlag0002.setProjektnummer("2026-0002");
            vorschlag0002.setAbrechnungBeginn(BEGINN);
            vorschlag0002.setAbrechnungEnde(ENDE);

            when(projektRepository.existsById(projektId)).thenReturn(true);
            when(projektRepository.findVorgaengerVorschlaege(Set.of(projektId))).thenReturn(List.of(
                    new VorgaengerVorschlag(projektId, vorschlag0003),
                    new VorgaengerVorschlag(projektId, vorschlag0002)));
            when(projektRepository.findVorgaengerVorschlaege(Set.of(vorschlag0003Id, vorschlag0002Id))).thenReturn(List.of());

            final List<Projekt> result = unitUnderTest.getVorgaengerVorschlaege(projektId);

            assertThat(result).extracting(Projekt::projektnummer).containsExactly("2026-0003", "2026-0002");
        }

        @Test
        void givenVorschlagSharingSeveralAdressen_thenReturnItOnce() {
            final UUID projektId = UUID.randomUUID();

            final UUID vorschlagId = UUID.randomUUID();
            final ProjektEntity vorschlag = new ProjektEntity();
            vorschlag.setId(vorschlagId);
            vorschlag.setProjektnummer("2026-0002");
            vorschlag.setAbrechnungBeginn(BEGINN);
            vorschlag.setAbrechnungEnde(ENDE);

            when(projektRepository.existsById(projektId)).thenReturn(true);
            when(projektRepository.findVorgaengerVorschlaege(Set.of(projektId))).thenReturn(List.of(
                    new VorgaengerVorschlag(projektId, vorschlag),
                    new VorgaengerVorschlag(projektId, vorschlag)));
            when(projektRepository.findVorgaengerVorschlaege(Set.of(vorschlagId))).thenReturn(List.of());

            final List<Projekt> result = unitUnderTest.getVorgaengerVorschlaege(projektId);

            assertThat(result).extracting(Projekt::projektnummer).containsExactly("2026-0002");
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            when(projektRepository.existsById(projektId)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.getVorgaengerVorschlaege(projektId))
                    .withMessageContaining(projektId.toString());
            verify(projektRepository, never()).findVorgaengerVorschlaege(any());
        }
    }

    @Nested
    class LinkToVorgaenger {
        @Test
        void givenProjektItself_thenThrowBadRequestWithoutAccessingTheRepository() {
            final UUID projektId = UUID.randomUUID();

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, projektId))
                    .withMessageContaining(projektId.toString());
            verifyNoInteractions(projektRepository);
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            final UUID vorgaengerId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, vorgaengerId))
                    .withMessageContaining(projektId.toString());
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenProjektWithVorgaenger_thenThrowConflictAndKeepItsVorgaenger() {
            final UUID bisherigerVorgaengerId = UUID.randomUUID();
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setVorgaengerProjektId(bisherigerVorgaengerId);
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, UUID.randomUUID()))
                    .withMessageContaining(projektId.toString());
            assertThat(projektEntity.getVorgaengerProjektId()).isEqualTo(bisherigerVorgaengerId);
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenProjektWithNachfolger_thenThrowConflict() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0001");
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(true);

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, UUID.randomUUID()))
                    .withMessageContaining(projektId.toString());
            assertThat(projektEntity.getVorgaengerProjektId()).isNull();
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenVorgaengerWithNachfolger_thenThrowConflict() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0003");

            final UUID vorgaengerId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(false);
            when(projektRepository.existsById(vorgaengerId)).thenReturn(true);
            when(projektRepository.existsByVorgaengerProjektId(vorgaengerId)).thenReturn(true);

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, vorgaengerId))
                    .withMessageContaining(vorgaengerId.toString());
            assertThat(projektEntity.getVorgaengerProjektId()).isNull();
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenUnknownVorgaenger_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0003");

            final UUID unknownId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(false);
            when(projektRepository.existsById(unknownId)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.linkToVorgaenger(projektId, unknownId))
                    .withMessageContaining(unknownId.toString());
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenStandaloneProjektAndLastLinkedProjekt_thenLinkItBehindTheLastOne() {
            final UUID projekt0001Id = UUID.randomUUID();
            final ProjektEntity projekt0001 = new ProjektEntity();
            projekt0001.setId(projekt0001Id);
            projekt0001.setProjektnummer("2026-0001");

            final UUID projekt0002Id = UUID.randomUUID();
            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setId(projekt0002Id);
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setVorgaengerProjektId(projekt0001Id);

            final UUID projekt0003Id = UUID.randomUUID();
            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setId(projekt0003Id);
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);

            when(projektRepository.findById(projekt0003Id)).thenReturn(Optional.of(projekt0003));
            when(projektRepository.existsByVorgaengerProjektId(projekt0003Id)).thenReturn(false);
            when(projektRepository.existsById(projekt0002Id)).thenReturn(true);
            when(projektRepository.existsByVorgaengerProjektId(projekt0002Id)).thenReturn(false);
            when(projektRepository.save(projekt0003)).thenReturn(projekt0003);
            when(projektRepository.findAllById(Set.of(projekt0002Id))).thenReturn(List.of(projekt0002));
            when(projektRepository.findAllById(Set.of(projekt0001Id))).thenReturn(List.of(projekt0001));

            final Projekt result = unitUnderTest.linkToVorgaenger(projekt0003Id, projekt0002Id);

            final ArgumentCaptor<ProjektEntity> savedCaptor = ArgumentCaptor.forClass(ProjektEntity.class);
            verify(projektRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().getVorgaengerProjektId()).isEqualTo(projekt0002Id);
            assertThat(result.vorgaengerProjekte()).containsExactly(
                    new LinkedProjekt(projekt0001Id, "2026-0001"),
                    new LinkedProjekt(projekt0002Id, "2026-0002"));
        }

        @Test
        void givenAbgelehnteVerknuepfung_thenWithdrawTheDecision() {
            final UUID vorgaengerId = UUID.randomUUID();
            final ProjektEntity vorgaenger = new ProjektEntity();
            vorgaenger.setId(vorgaengerId);
            vorgaenger.setProjektnummer("2026-0001");

            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);
            projektEntity.setVerknuepfungAbgelehnt(true);

            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(false);
            when(projektRepository.existsById(vorgaengerId)).thenReturn(true);
            when(projektRepository.existsByVorgaengerProjektId(vorgaengerId)).thenReturn(false);
            when(projektRepository.save(projektEntity)).thenReturn(projektEntity);
            when(projektRepository.findAllById(Set.of(vorgaengerId))).thenReturn(List.of(vorgaenger));

            final Projekt result = unitUnderTest.linkToVorgaenger(projektId, vorgaengerId);

            final ArgumentCaptor<ProjektEntity> savedCaptor = ArgumentCaptor.forClass(ProjektEntity.class);
            verify(projektRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().isVerknuepfungAbgelehnt()).isFalse();
            assertThat(result.verknuepfungAbgelehnt()).isFalse();
        }
    }

    @Nested
    class DeclineVorgaenger {
        @Test
        void givenStandaloneProjekt_thenStoreTheDecision() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(false);
            when(projektRepository.save(projektEntity)).thenReturn(projektEntity);

            final Projekt result = unitUnderTest.declineVorgaenger(projektId);

            final ArgumentCaptor<ProjektEntity> savedCaptor = ArgumentCaptor.forClass(ProjektEntity.class);
            verify(projektRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().isVerknuepfungAbgelehnt()).isTrue();
            assertThat(result.verknuepfungAbgelehnt()).isTrue();
        }

        @Test
        void givenProjektWithVorgaenger_thenThrowConflictAndStoreNothing() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setVorgaengerProjektId(UUID.randomUUID());
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.declineVorgaenger(projektId))
                    .withMessageContaining(projektId.toString());
            assertThat(projektEntity.isVerknuepfungAbgelehnt()).isFalse();
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenProjektWithNachfolger_thenThrowConflictAndStoreNothing() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0001");
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(true);

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.declineVorgaenger(projektId))
                    .withMessageContaining(projektId.toString());
            assertThat(projektEntity.isVerknuepfungAbgelehnt()).isFalse();
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.declineVorgaenger(projektId))
                    .withMessageContaining(projektId.toString());
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }
    }

    @Nested
    class UnlinkFromVorgaenger {
        @Test
        void givenLastLinkedProjekt_thenClearItsPointer() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setVorgaengerProjektId(UUID.randomUUID());
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(false);

            unitUnderTest.unlinkFromVorgaenger(projektId);

            final ArgumentCaptor<ProjektEntity> savedCaptor = ArgumentCaptor.forClass(ProjektEntity.class);
            verify(projektRepository).save(savedCaptor.capture());
            assertThat(savedCaptor.getValue().getVorgaengerProjektId()).isNull();
        }

        @Test
        void givenProjektWithNachfolger_thenThrowConflictAndKeepItsVorgaenger() {
            final UUID vorgaengerId = UUID.randomUUID();
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setVorgaengerProjektId(vorgaengerId);
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));
            when(projektRepository.existsByVorgaengerProjektId(projektId)).thenReturn(true);

            assertThatExceptionOfType(ConflictException.class)
                    .isThrownBy(() -> unitUnderTest.unlinkFromVorgaenger(projektId))
                    .withMessageContaining(projektId.toString());
            assertThat(projektEntity.getVorgaengerProjektId()).isEqualTo(vorgaengerId);
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            final UUID projektId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> unitUnderTest.unlinkFromVorgaenger(projektId))
                    .withMessageContaining(projektId.toString());
            verify(projektRepository, never()).save(any(ProjektEntity.class));
        }
    }

    @Nested
    class GetVorgaengerProjektId {
        @Test
        void givenProjektWithVorgaenger_thenReturnTheVorgaenger() {
            final UUID vorgaengerId = UUID.randomUUID();
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0002");
            projektEntity.setVorgaengerProjektId(vorgaengerId);
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));

            assertThat(unitUnderTest.getVorgaengerProjektId(projektId)).isEqualTo(vorgaengerId);
        }

        @Test
        void givenProjektWithoutVorgaenger_thenReturnNull() {
            final UUID projektId = UUID.randomUUID();
            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(projektId);
            projektEntity.setProjektnummer("2026-0001");
            when(projektRepository.findById(projektId)).thenReturn(Optional.of(projektEntity));

            assertThat(unitUnderTest.getVorgaengerProjektId(projektId)).isNull();
        }

        @Test
        void givenUnknownProjekt_thenReturnNull() {
            final UUID projektId = UUID.randomUUID();
            when(projektRepository.findById(projektId)).thenReturn(Optional.empty());

            assertThat(unitUnderTest.getVorgaengerProjektId(projektId)).isNull();
        }
    }
}
