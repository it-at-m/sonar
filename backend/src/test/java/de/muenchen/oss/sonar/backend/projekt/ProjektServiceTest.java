package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjektServiceTest {

    private static final String DEFAULT_PROJEKTNUMMER = "2026-0001";
    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Mock
    private ProjektRepository projektRepository;

    @Spy
    private final ProjektEntityMapper projektEntityMapper = Mappers.getMapper(ProjektEntityMapper.class);

    @InjectMocks
    private ProjektService unitUnderTest;

    @Nested
    class CreateProjekt {
        @Test
        void givenProjekt_thenReturnSavedProjekt() {
            // Given
            final ProjektAdresse adresse = new ProjektAdresse(
                    null, "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, true);
            final Projekt projekt = new Projekt(null, DEFAULT_PROJEKTNUMMER, BEGINN, ENDE, List.of(adresse));

            final UUID savedId = UUID.randomUUID();
            when(projektRepository.save(any(ProjektEntity.class))).thenAnswer(invocation -> {
                final ProjektEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            // When
            final Projekt result = unitUnderTest.createProjekt(projekt);

            // Then
            verify(projektRepository).save(any(ProjektEntity.class));
            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.projektnummer()).isEqualTo(DEFAULT_PROJEKTNUMMER);
            assertThat(result.adressen()).hasSize(1);
            assertThat(result.adressen().getFirst().bezeichnung()).isEqualTo("Flurstück 1234/5");
            assertThat(result.adressen().getFirst().sondernutzungErlaubt()).isTrue();
        }
    }
}
