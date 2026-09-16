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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbrechnungServiceTest {

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
    private AbrechnungService unitUnderTest;

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
