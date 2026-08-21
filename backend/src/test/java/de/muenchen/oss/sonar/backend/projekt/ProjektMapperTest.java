package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseResponseDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektMapper;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektAdresseView;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProjektMapperTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private final ProjektMapper projektMapper = Mappers.getMapper(ProjektMapper.class);

    @Nested
    class ToDTO {
        @Test
        void givenView_thenReturnsCorrectDTO() {
            // Given
            final ProjektAdresseView adresseView = new ProjektAdresseView(
                    UUID.randomUUID(), "Marienplatz 8", "Gastronomie", BEGINN, ENDE, 90, 2, true);
            final ProjektView view = new ProjektView(
                    UUID.randomUUID(), "2026-0001", BEGINN, ENDE, List.of(adresseView));

            // When
            final ProjektResponseDTO result = projektMapper.toDTO(view);

            // Then
            assertNotNull(result);
            assertThat(result.id()).isEqualTo(view.id());
            assertThat(result.projektnummer()).isEqualTo(view.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(view.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(view.abrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresseResponseDTO adresseDTO = result.adressen().getFirst();
            assertThat(adresseDTO.id()).isEqualTo(adresseView.id());
            assertThat(adresseDTO.bezeichnung()).isEqualTo(adresseView.bezeichnung());
            assertThat(adresseDTO.baunutzung()).isEqualTo(adresseView.baunutzung());
            assertThat(adresseDTO.unerlaubteNutzungVon()).isEqualTo(adresseView.unerlaubteNutzungVon());
            assertThat(adresseDTO.unerlaubteNutzungBis()).isEqualTo(adresseView.unerlaubteNutzungBis());
            assertThat(adresseDTO.tageUnerlaubteNutzung()).isEqualTo(adresseView.tageUnerlaubteNutzung());
            assertThat(adresseDTO.anzahlMahnungen()).isEqualTo(adresseView.anzahlMahnungen());
            assertThat(adresseDTO.sondernutzungErlaubt()).isEqualTo(adresseView.sondernutzungErlaubt());
        }
    }

    @Nested
    class ToCreateCommand {
        @Test
        void givenRequestDTO_thenReturnsCorrectCommand() {
            // Given
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            // When
            final CreateProjektCommand result = projektMapper.toCreateCommand(requestDTO);

            // Then
            assertNotNull(result);
            assertThat(result.projektnummer()).isEqualTo(requestDTO.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(requestDTO.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(requestDTO.abrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final CreateProjektCommand.Adresse adresseCommand = result.adressen().getFirst();
            assertThat(adresseCommand.bezeichnung()).isEqualTo(adresseDTO.bezeichnung());
            assertThat(adresseCommand.baunutzung()).isEqualTo(adresseDTO.baunutzung());
            assertThat(adresseCommand.unerlaubteNutzungVon()).isEqualTo(adresseDTO.unerlaubteNutzungVon());
            assertThat(adresseCommand.unerlaubteNutzungBis()).isEqualTo(adresseDTO.unerlaubteNutzungBis());
            assertThat(adresseCommand.tageUnerlaubteNutzung()).isEqualTo(adresseDTO.tageUnerlaubteNutzung());
            assertThat(adresseCommand.anzahlMahnungen()).isEqualTo(adresseDTO.anzahlMahnungen());
            assertThat(adresseCommand.sondernutzungErlaubt()).isEqualTo(adresseDTO.sondernutzungErlaubt());
        }
    }

}
