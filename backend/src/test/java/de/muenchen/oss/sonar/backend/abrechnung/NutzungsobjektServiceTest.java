package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.abrechnung.domain.NutzungsobjektSuggestion;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private static final UUID VORGAENGER_PROJEKT_ID = UUID.randomUUID();
    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    @Mock
    private AbrechnungRepository abrechnungRepository;

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

            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(PROJEKT_ID, nutzungsobjekt)));

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).singleElement().satisfies(suggestion -> {
                assertThat(suggestion.nutzungsobjekt().id()).isEqualTo(nutzungsobjekt.getId());
                assertThat(suggestion.nutzungsobjekt().adresse()).isEqualTo("Marienplatz");
                assertThat(suggestion.nutzungsobjekt().bemerkung()).isEqualTo("Aus der Erstabrechnung");
                assertThat(suggestion.nutzungsobjekt().aufschlag50prozent()).isTrue();
                assertThat(suggestion.nutzungsobjekt().positionen()).isEmpty();
                assertThat(suggestion.vorgaengerProjektnummer()).isNull();
            });
        }

        @Test
        void givenNutzungsobjektOnlyOfAVorgaenger_thenNameThatVorgaenger() {
            final NutzungsobjektEntity nutzungsobjekt = new NutzungsobjektEntity();
            nutzungsobjekt.setId(UUID.randomUUID());
            nutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            nutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO,
                    List.of(), VORGAENGER_PROJEKT_ID, List.of(new LinkedProjekt(VORGAENGER_PROJEKT_ID, "2026-0001")), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, VORGAENGER_PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, nutzungsobjekt)));

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).singleElement().extracting(NutzungsobjektSuggestion::vorgaengerProjektnummer).isEqualTo("2026-0001");
        }

        @Test
        void givenNutzungsobjektThatTheProjektBillsToo_thenNameNoProjekt() {
            final NutzungsobjektEntity nutzungsobjekt = new NutzungsobjektEntity();
            nutzungsobjekt.setId(UUID.randomUUID());
            nutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            nutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO,
                    List.of(), VORGAENGER_PROJEKT_ID, List.of(new LinkedProjekt(VORGAENGER_PROJEKT_ID, "2026-0001")), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, VORGAENGER_PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, nutzungsobjekt),
                            new NutzungsobjektImProjekt(PROJEKT_ID, nutzungsobjekt)));

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).singleElement().extracting(NutzungsobjektSuggestion::vorgaengerProjektnummer).isNull();
        }

        @Test
        void givenNutzungsobjektOfSeveralVorgaenger_thenNameTheNearestOne() {
            final UUID ersterVorgaengerId = UUID.randomUUID();
            final UUID zweiterVorgaengerId = UUID.randomUUID();
            final NutzungsobjektEntity nutzungsobjekt = new NutzungsobjektEntity();
            nutzungsobjekt.setId(UUID.randomUUID());
            nutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            nutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0003", VON, BIS, BigDecimal.ZERO,
                    List.of(), zweiterVorgaengerId, List.of(new LinkedProjekt(ersterVorgaengerId, "2026-0001"),
                            new LinkedProjekt(zweiterVorgaengerId, "2026-0002")),
                    List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, ersterVorgaengerId, zweiterVorgaengerId)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(ersterVorgaengerId, nutzungsobjekt),
                            new NutzungsobjektImProjekt(zweiterVorgaengerId, nutzungsobjekt)));

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).singleElement().extracting(NutzungsobjektSuggestion::vorgaengerProjektnummer).isEqualTo("2026-0002");
        }

        @Test
        void givenSeveralNutzungsobjekte_thenKeepTheOrderOfTheQuery() {
            final NutzungsobjektEntity marienplatz = new NutzungsobjektEntity();
            marienplatz.setId(UUID.randomUUID());
            marienplatz.getAdressdaten().setArt(Adressart.ADRESSE);
            marienplatz.getAdressdaten().setAdresse("Marienplatz");

            final NutzungsobjektEntity sendlingerStrasse = new NutzungsobjektEntity();
            sendlingerStrasse.setId(UUID.randomUUID());
            sendlingerStrasse.getAdressdaten().setArt(Adressart.ADRESSE);
            sendlingerStrasse.getAdressdaten().setAdresse("Sendlinger Straße");

            final NutzungsobjektEntity tal = new NutzungsobjektEntity();
            tal.setId(UUID.randomUUID());
            tal.getAdressdaten().setArt(Adressart.ADRESSE);
            tal.getAdressdaten().setAdresse("Tal");

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO,
                    List.of(), VORGAENGER_PROJEKT_ID, List.of(new LinkedProjekt(VORGAENGER_PROJEKT_ID, "2026-0001")), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, VORGAENGER_PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(PROJEKT_ID, marienplatz),
                            new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, sendlingerStrasse),
                            new NutzungsobjektImProjekt(PROJEKT_ID, sendlingerStrasse),
                            new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, tal)));

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).extracting(suggestion -> suggestion.nutzungsobjekt().adresse())
                    .containsExactly("Marienplatz", "Sendlinger Straße", "Tal");
        }

        @Test
        void givenProjektWithNachfolger_thenLeaveTheNutzungsobjekteOfTheNachfolgerOut() {
            final UUID nachfolgerProjektId = UUID.randomUUID();
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, BigDecimal.ZERO,
                    List.of(), null, List.of(), List.of(new LinkedProjekt(nachfolgerProjektId, "2026-0002")), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID))).thenReturn(List.of());

            final List<NutzungsobjektSuggestion> result = unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFound() {
            when(projektService.getProjekt(PROJEKT_ID)).thenThrow(new NotFoundException("Could not find entity with id " + PROJEKT_ID));

            assertThatThrownBy(() -> unitUnderTest.getNutzungsobjekteOfProjekt(PROJEKT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(PROJEKT_ID.toString());
        }
    }

    @Nested
    class SelectedPreexistingNutzungsobjekte {
        @Test
        void givenNamedNutzungsobjekt_thenReturnTheStoredOne() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity gespeichertesNutzungsobjekt = new NutzungsobjektEntity();
            gespeichertesNutzungsobjekt.setId(nutzungsobjektId);
            gespeichertesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            gespeichertesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            final NutzungsobjektEntity anderesNutzungsobjekt = new NutzungsobjektEntity();
            anderesNutzungsobjekt.setId(UUID.randomUUID());
            anderesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            anderesNutzungsobjekt.getAdressdaten().setAdresse("Sendlinger Straße");

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(PROJEKT_ID, gespeichertesNutzungsobjekt),
                            new NutzungsobjektImProjekt(PROJEKT_ID, anderesNutzungsobjekt)));

            final Map<UUID, NutzungsobjektEntity> result = unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung);

            assertThat(result).containsExactly(Map.entry(nutzungsobjektId, gespeichertesNutzungsobjekt));
        }

        @Test
        void givenNutzungsobjektOfAVorgaenger_thenReturnTheStoredOne() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity nutzungsobjektDesVorgaengers = new NutzungsobjektEntity();
            nutzungsobjektDesVorgaengers.setId(nutzungsobjektId);
            nutzungsobjektDesVorgaengers.getAdressdaten().setArt(Adressart.ADRESSE);
            nutzungsobjektDesVorgaengers.getAdressdaten().setAdresse("Marienplatz");

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO,
                    List.of(), VORGAENGER_PROJEKT_ID, List.of(new LinkedProjekt(VORGAENGER_PROJEKT_ID, "2026-0001")), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, VORGAENGER_PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, nutzungsobjektDesVorgaengers)));

            final Map<UUID, NutzungsobjektEntity> result = unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung);

            assertThat(result).containsExactly(Map.entry(nutzungsobjektId, nutzungsobjektDesVorgaengers));
        }

        @Test
        void givenNutzungsobjektOfTheProjektAndOfAVorgaenger_thenReturnItOnce() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity gemeinsamesNutzungsobjekt = new NutzungsobjektEntity();
            gemeinsamesNutzungsobjekt.setId(nutzungsobjektId);
            gemeinsamesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            gemeinsamesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0002", VON, BIS, BigDecimal.ZERO,
                    List.of(), VORGAENGER_PROJEKT_ID, List.of(new LinkedProjekt(VORGAENGER_PROJEKT_ID, "2026-0001")), List.of(), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID, VORGAENGER_PROJEKT_ID)))
                    .thenReturn(List.of(new NutzungsobjektImProjekt(VORGAENGER_PROJEKT_ID, gemeinsamesNutzungsobjekt),
                            new NutzungsobjektImProjekt(PROJEKT_ID, gemeinsamesNutzungsobjekt)));

            final Map<UUID, NutzungsobjektEntity> result = unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung);

            assertThat(result).containsExactly(Map.entry(nutzungsobjektId, gemeinsamesNutzungsobjekt));
        }

        @Test
        void givenNutzungsobjektOfANachfolger_thenThrowNotFound() {
            final UUID nachfolgerProjektId = UUID.randomUUID();
            final UUID nutzungsobjektId = UUID.randomUUID();
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, BigDecimal.ZERO,
                    List.of(), null, List.of(), List.of(new LinkedProjekt(nachfolgerProjektId, "2026-0002")), false, false));
            when(abrechnungRepository.findNutzungsobjekteImProjektByProjektIdIn(List.of(PROJEKT_ID))).thenReturn(List.of());

            assertThatThrownBy(() -> unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nutzungsobjektId.toString());
        }

        @Test
        void givenOnlyNewNutzungsobjekte_thenLookUpNothing() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            assertThat(unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung)).isEmpty();
            verify(projektService, never()).getProjekt(any(UUID.class));
            verify(abrechnungRepository, never()).findNutzungsobjekteImProjektByProjektIdIn(any());
        }
    }
}
