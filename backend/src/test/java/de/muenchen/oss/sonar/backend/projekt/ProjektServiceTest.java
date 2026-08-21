package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektModelMapper;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
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
    private final ProjektModelMapper projektModelMapper = Mappers.getMapper(ProjektModelMapper.class);

    @InjectMocks
    private ProjektService unitUnderTest;

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
