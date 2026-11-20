package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutzungsobjektServiceTest {

    private static final UUID PROJEKT_ID = UUID.randomUUID();
    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    @Mock
    private AbrechnungRepository abrechnungRepository;

    @Mock
    private NutzungsobjektRepository nutzungsobjektRepository;

    @Mock
    private ProjektService projektService;

    @Spy
    private final AbrechnungEntityMapper abrechnungEntityMapper = Mappers.getMapper(AbrechnungEntityMapper.class);

    @InjectMocks
    private NutzungsobjektService unitUnderTest;

    @Nested
    class GetNutzungsobjekteOfProjekt {
        @Test
        void givenProjekt_thenReturnItsNutzungsobjekteWithoutPositionen() {
            final NutzungsobjektEntity nutzungsobjekt = new NutzungsobjektEntity();
            nutzungsobjekt.setId(UUID.randomUUID());
            nutzungsobjekt.setBemerkung("Aus der Erstabrechnung");
            nutzungsobjekt.setAufschlag50prozent(true);
            nutzungsobjekt.addNutzung(Nutzung.BAUZAUN);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(PROJEKT_ID)).thenReturn(List.of(nutzungsobjekt));

            final List<AbrechnungNutzungsobjekt> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(nutzungsobjekt.getId());
            assertThat(result.getFirst().adresse()).isEqualTo("Marienplatz");
            assertThat(result.getFirst().bemerkung()).isEqualTo("Aus der Erstabrechnung");
            assertThat(result.getFirst().aufschlag50prozent()).isTrue();
            assertThat(result.getFirst().positionen()).isEmpty();
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(PROJEKT_ID.toString());
        }
    }

    @Nested
    class AssignNutzungsobjekte {
        @Test
        void givenNewNutzungsobjekt_thenBuildItFromTheEnteredDetails() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, List.of(Nutzung.BAUZAUN), null,
                    VON, BIS, null, "Bemerkung", true, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung);

            final NutzungsobjektEntity neuesNutzungsobjekt = abrechnungEntity.getNutzungsobjekte().getFirst();
            assertThat(neuesNutzungsobjekt.getId()).isNull();
            assertThat(neuesNutzungsobjekt.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(neuesNutzungsobjekt.getAdressdaten().getHausnummerBis()).isEqualTo("12");
            assertThat(neuesNutzungsobjekt.getAdressdaten().getTageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(neuesNutzungsobjekt.getNutzungen()).containsExactly(Nutzung.BAUZAUN);
            assertThat(neuesNutzungsobjekt.getBemerkung()).isEqualTo("Bemerkung");
            assertThat(neuesNutzungsobjekt.isAufschlag50prozent()).isTrue();
            assertThat(abrechnungEntity.getPositionen()).extracting(AbrechnungPositionEntity::getFlaeche)
                    .containsExactly(new BigDecimal("36.00"));
            assertThat(abrechnungEntity.getPositionen().getFirst().getNutzungsobjekt()).isSameAs(neuesNutzungsobjekt);
        }

        @Test
        void givenSeveralNewNutzungsobjekte_thenLinkEachPositionToItsOwnNutzungsobjekt() {
            final AbrechnungPosition positionAmMarienplatz = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt marienplatz = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, true, List.of(positionAmMarienplatz), List.of());
            final AbrechnungPosition positionInDerSendlingerStrasse = new AbrechnungPosition(null, VON, BIS, new BigDecimal("15.00"),
                    new BigDecimal("3.00"), new BigDecimal("45.00"));
            final AbrechnungNutzungsobjekt sendlingerStrasse = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, List.of(Nutzung.CONTAINER), null,
                    null, null, null, null, false, List.of(positionInDerSendlingerStrasse), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(marienplatz, sendlingerStrasse));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung);

            assertThat(abrechnungEntity.getNutzungsobjekte())
                    .extracting(nutzungsobjekt -> nutzungsobjekt.getAdressdaten().getAdresse())
                    .containsExactly("Marienplatz", "Sendlinger Straße");
            assertThat(abrechnungEntity.getPositionen().getFirst().getNutzungsobjekt())
                    .isSameAs(abrechnungEntity.getNutzungsobjekte().getFirst());
            assertThat(abrechnungEntity.getPositionen().getLast().getNutzungsobjekt())
                    .isSameAs(abrechnungEntity.getNutzungsobjekte().getLast());
        }

        @Test
        void givenNutzungsobjektToTakeOver_thenBillTheStoredOneUnchanged() {
            final UUID uebernommenesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity uebernommenesNutzungsobjekt = new NutzungsobjektEntity();
            uebernommenesNutzungsobjekt.setId(uebernommenesNutzungsobjektId);
            uebernommenesNutzungsobjekt.addNutzung(Nutzung.BAUZAUN);
            uebernommenesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            uebernommenesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");
            uebernommenesNutzungsobjekt.getAdressdaten().setHausnummerVon("8");

            final NutzungsobjektEntity anderesNutzungsobjekt = new NutzungsobjektEntity();
            anderesNutzungsobjekt.setId(UUID.randomUUID());
            anderesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            anderesNutzungsobjekt.getAdressdaten().setAdresse("Sendlinger Straße");

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    uebernommenesNutzungsobjektId, false, Adressart.ADRESSE, "Wird ignoriert", "1", null, null, null,
                    List.of(Nutzung.CONTAINER), null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            when(abrechnungRepository.findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(PROJEKT_ID))
                    .thenReturn(List.of(uebernommenesNutzungsobjekt, anderesNutzungsobjekt));

            unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung);

            assertThat(abrechnungEntity.getNutzungsobjekte()).containsExactly(uebernommenesNutzungsobjekt);
            assertThat(uebernommenesNutzungsobjekt.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(uebernommenesNutzungsobjekt.getNutzungen()).containsExactly(Nutzung.BAUZAUN);
            assertThat(abrechnungEntity.getPositionen().getFirst().getNutzungsobjekt()).isSameAs(uebernommenesNutzungsobjekt);
        }

        @Test
        void givenNutzungsobjektThatNoFinalizedAbrechnungBills_thenThrowNotFound() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, false, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            when(abrechnungRepository.findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(PROJEKT_ID)).thenReturn(List.of());

            assertThatThrownBy(() -> unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nutzungsobjektId.toString());
        }

        @Test
        void givenOnlyNewNutzungsobjekte_thenLookUpNothing() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung);

            verify(abrechnungRepository, never()).findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(any(UUID.class));
            verify(abrechnungRepository, never()).findNutzungsobjektIdsOfFinalizedAbrechnungen(anyCollection());
        }

        @Test
        void givenOwnNutzungsobjektWithItsId_thenUpdateItInPlace() {
            final UUID eigenesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity eigenesNutzungsobjekt = new NutzungsobjektEntity();
            eigenesNutzungsobjekt.setId(eigenesNutzungsobjektId);
            eigenesNutzungsobjekt.setBemerkung("Erste Fassung");
            eigenesNutzungsobjekt.addNutzung(Nutzung.BAUZAUN);
            eigenesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            eigenesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");
            eigenesNutzungsobjekt.getAdressdaten().setHausnummerVon("8");

            final AbrechnungEntity draft = new AbrechnungEntity();
            draft.setDraft(true);
            draft.addNutzungsobjekt(eigenesNutzungsobjekt);

            final AbrechnungNutzungsobjekt geaendertesNutzungsobjekt = new AbrechnungNutzungsobjekt(
                    eigenesNutzungsobjektId, true, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null,
                    List.of(Nutzung.CONTAINER), null, null, null, null, "Zweite Fassung", false, List.of(), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(geaendertesNutzungsobjekt));

            unitUnderTest.assignNutzungsobjekte(draft, abrechnung);

            assertThat(draft.getNutzungsobjekte()).containsExactly(eigenesNutzungsobjekt);
            assertThat(eigenesNutzungsobjekt.getAdressdaten().getAdresse()).isEqualTo("Sendlinger Straße");
            assertThat(eigenesNutzungsobjekt.getNutzungen()).containsExactly(Nutzung.CONTAINER);
            assertThat(eigenesNutzungsobjekt.getBemerkung()).isEqualTo("Zweite Fassung");
            verify(abrechnungRepository, never()).findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(any(UUID.class));
        }

        @Test
        void givenOwnNutzungsobjektIdForANewAbrechnung_thenThrowNotFound() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, true, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            assertThatThrownBy(() -> unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nutzungsobjektId.toString());
        }

        @Test
        void givenTakenOverNutzungsobjektSentAsOwn_thenThrowNotFoundAndLeaveItAsItIs() {
            final UUID uebernommenesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity uebernommenesNutzungsobjekt = new NutzungsobjektEntity();
            uebernommenesNutzungsobjekt.setId(uebernommenesNutzungsobjektId);
            uebernommenesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            uebernommenesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");
            uebernommenesNutzungsobjekt.getAdressdaten().setHausnummerVon("8");

            final AbrechnungEntity draft = new AbrechnungEntity();
            draft.setDraft(true);
            draft.addNutzungsobjekt(uebernommenesNutzungsobjekt);

            final AbrechnungNutzungsobjekt alsEigenesGesendet = new AbrechnungNutzungsobjekt(
                    uebernommenesNutzungsobjektId, true, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null,
                    List.of(Nutzung.CONTAINER), null, null, null, null, null, false, List.of(), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(alsEigenesGesendet));

            when(abrechnungRepository.findNutzungsobjektIdsOfFinalizedAbrechnungen(List.of(uebernommenesNutzungsobjektId)))
                    .thenReturn(Set.of(uebernommenesNutzungsobjektId));

            assertThatThrownBy(() -> unitUnderTest.assignNutzungsobjekte(draft, abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(uebernommenesNutzungsobjektId.toString());
            assertThat(uebernommenesNutzungsobjekt.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
        }

        @Test
        void givenPositionAndMastWithTheirIds_thenUpdateThemInPlace() {
            final UUID positionId = UUID.randomUUID();
            final AbrechnungPositionEntity bisherigePosition = new AbrechnungPositionEntity();
            bisherigePosition.setId(positionId);
            bisherigePosition.setBeginn(VON);
            bisherigePosition.setEnde(BIS);
            bisherigePosition.setFlaeche(new BigDecimal("36.00"));

            final UUID mastId = UUID.randomUUID();
            final AbrechnungMastEntity bisherigerMast = new AbrechnungMastEntity();
            bisherigerMast.setId(mastId);
            bisherigerMast.setBeginn(VON);
            bisherigerMast.setEnde(BIS);

            final NutzungsobjektEntity bisherigesNutzungsobjekt = new NutzungsobjektEntity();
            bisherigesNutzungsobjekt.setId(UUID.randomUUID());

            final AbrechnungEntity draft = new AbrechnungEntity();
            draft.setDraft(true);
            bisherigePosition.setNutzungsobjekt(bisherigesNutzungsobjekt);
            bisherigerMast.setNutzungsobjekt(bisherigesNutzungsobjekt);
            draft.addNutzungsobjekt(bisherigesNutzungsobjekt);
            draft.addPosition(bisherigePosition);
            draft.addMast(bisherigerMast);

            final LocalDate mastEnde = LocalDate.of(2026, 2, 28);
            final AbrechnungPosition geaendertePosition = new AbrechnungPosition(positionId, VON, BIS, new BigDecimal("15.00"),
                    new BigDecimal("3.00"), new BigDecimal("45.00"));
            final AbrechnungMast geaenderterMast = new AbrechnungMast(mastId, VON, mastEnde);
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(geaendertePosition), List.of(geaenderterMast));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            unitUnderTest.assignNutzungsobjekte(draft, abrechnung);

            assertThat(draft.getPositionen()).containsExactly(bisherigePosition);
            assertThat(bisherigePosition.getId()).isEqualTo(positionId);
            assertThat(bisherigePosition.getFlaeche()).isEqualByComparingTo("45.00");
            assertThat(bisherigePosition.getNutzungsobjekt()).isSameAs(draft.getNutzungsobjekte().getFirst());
            assertThat(draft.getMasten()).containsExactly(bisherigerMast);
            assertThat(bisherigerMast.getEnde()).isEqualTo(mastEnde);
        }

        @Test
        void givenPositionIdTheAbrechnungDoesNotHave_thenThrowNotFound() {
            final UUID fremdePositionId = UUID.randomUUID();
            final AbrechnungPosition fremdePosition = new AbrechnungPosition(fremdePositionId, VON, BIS, null, null,
                    new BigDecimal("45.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, true, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(fremdePosition), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, false, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity abrechnungEntity = new AbrechnungEntity();

            assertThatThrownBy(() -> unitUnderTest.assignNutzungsobjekte(abrechnungEntity, abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(fremdePositionId.toString());
        }

    }

    @Nested
    class OwnNutzungsobjekte {
        @Test
        void givenDraftWithATakenOverNutzungsobjekt_thenLeaveItOut() {
            final UUID eigenesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity eigenesNutzungsobjekt = new NutzungsobjektEntity();
            eigenesNutzungsobjekt.setId(eigenesNutzungsobjektId);

            final UUID uebernommenesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity uebernommenesNutzungsobjekt = new NutzungsobjektEntity();
            uebernommenesNutzungsobjekt.setId(uebernommenesNutzungsobjektId);

            final AbrechnungEntity draft = new AbrechnungEntity();
            draft.setDraft(true);
            draft.addNutzungsobjekt(eigenesNutzungsobjekt);
            draft.addNutzungsobjekt(uebernommenesNutzungsobjekt);

            when(abrechnungRepository.findNutzungsobjektIdsOfFinalizedAbrechnungen(
                    List.of(eigenesNutzungsobjektId, uebernommenesNutzungsobjektId)))
                    .thenReturn(Set.of(uebernommenesNutzungsobjektId));

            assertThat(unitUnderTest.ownNutzungsobjekte(draft))
                    .containsExactly(Map.entry(eigenesNutzungsobjektId, eigenesNutzungsobjekt));
        }
    }

    @Nested
    class DeleteUnbilled {
        @Test
        void givenNutzungsobjekte_thenDeleteOnlyThoseNoAbrechnungBills() {
            final UUID unbilledId = UUID.randomUUID();
            final UUID billedId = UUID.randomUUID();
            final NutzungsobjektEntity unbilled = new NutzungsobjektEntity();
            unbilled.setId(unbilledId);

            when(nutzungsobjektRepository.findUnbilledByIdIn(List.of(unbilledId, billedId))).thenReturn(List.of(unbilled));

            unitUnderTest.deleteUnbilled(List.of(unbilledId, billedId));

            verify(nutzungsobjektRepository).deleteAll(List.of(unbilled));
        }
    }
}
