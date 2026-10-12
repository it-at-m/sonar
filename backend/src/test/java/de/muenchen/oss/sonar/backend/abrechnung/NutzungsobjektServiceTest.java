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
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
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

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            when(projektService.existsProjekt(PROJEKT_ID)).thenReturn(true);
            when(abrechnungRepository.findNutzungsobjekteByProjektId(PROJEKT_ID)).thenReturn(List.of(nutzungsobjekt));

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
                    new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A,
                    null, null, null, null, false, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(abrechnungRepository.findNutzungsobjekteByProjektId(PROJEKT_ID))
                    .thenReturn(List.of(gespeichertesNutzungsobjekt, anderesNutzungsobjekt));

            final Map<UUID, NutzungsobjektEntity> result = unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung);

            assertThat(result).containsExactly(Map.entry(nutzungsobjektId, gespeichertesNutzungsobjekt));
        }

        @Test
        void givenNutzungsobjektOfAnotherProjekt_thenThrowNotFound() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    nutzungsobjektId, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A,
                    null, null, null, null, false, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            when(abrechnungRepository.findNutzungsobjekteByProjektId(PROJEKT_ID)).thenReturn(List.of());

            assertThatThrownBy(() -> unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nutzungsobjektId.toString());
        }

        @Test
        void givenOnlyNewNutzungsobjekte_thenLookUpNothing() {
            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A,
                    null, null, null, null, false, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, PROJEKT_ID, 0, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            assertThat(unitUnderTest.selectedPreexistingNutzungsobjekte(abrechnung)).isEmpty();
            verify(abrechnungRepository, never()).findNutzungsobjekteByProjektId(any(UUID.class));
        }
    }
}
