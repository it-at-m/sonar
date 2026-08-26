package de.muenchen.oss.sonar.backend.projekt.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.projekt.domain.ProjektAdresse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@AllArgsConstructor
class ProjektDTOMapperTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private final ProjektDTOMapper projektDTOMapper = Mappers.getMapper(ProjektDTOMapper.class);

    @Nested
    class ToDTO {
        @Test
        void givenProjekt_thenReturnsCorrectDTO() {
            // Given
            final ProjektAdresse adresse = new ProjektAdresse(
                    UUID.randomUUID(), "Marienplatz 8", "Gastronomie", BEGINN, ENDE, 90, 2, true);
            final Projekt projekt = new Projekt(
                    UUID.randomUUID(), "2026-0001", BEGINN, ENDE, List.of(adresse));

            // When
            final ProjektResponseDTO result = projektDTOMapper.toDTO(projekt);

            // Then
            assertNotNull(result);
            assertThat(result.id()).isEqualTo(projekt.id());
            assertThat(result.projektnummer()).isEqualTo(projekt.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(projekt.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(projekt.abrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresseResponseDTO adresseDTO = result.adressen().getFirst();
            assertThat(adresseDTO.id()).isEqualTo(adresse.id());
            assertThat(adresseDTO.bezeichnung()).isEqualTo(adresse.bezeichnung());
            assertThat(adresseDTO.baunutzung()).isEqualTo(adresse.baunutzung());
            assertThat(adresseDTO.unerlaubteNutzungVon()).isEqualTo(adresse.unerlaubteNutzungVon());
            assertThat(adresseDTO.unerlaubteNutzungBis()).isEqualTo(adresse.unerlaubteNutzungBis());
            assertThat(adresseDTO.tageUnerlaubteNutzung()).isEqualTo(adresse.tageUnerlaubteNutzung());
            assertThat(adresseDTO.anzahlMahnungen()).isEqualTo(adresse.anzahlMahnungen());
            assertThat(adresseDTO.sondernutzungErlaubt()).isEqualTo(adresse.sondernutzungErlaubt());
        }
    }

    @Nested
    class ToProjekt {
        @Test
        void givenRequestDTO_thenReturnsCorrectProjekt() {
            // Given
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            // When
            final Projekt result = projektDTOMapper.toProjekt(requestDTO);

            // Then
            assertNotNull(result);
            assertThat(result.id()).isNull();
            assertThat(result.projektnummer()).isEqualTo(requestDTO.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(requestDTO.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(requestDTO.abrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresse adresse = result.adressen().getFirst();
            assertThat(adresse.id()).isNull();
            assertThat(adresse.bezeichnung()).isEqualTo(adresseDTO.bezeichnung());
            assertThat(adresse.baunutzung()).isEqualTo(adresseDTO.baunutzung());
            assertThat(adresse.unerlaubteNutzungVon()).isEqualTo(adresseDTO.unerlaubteNutzungVon());
            assertThat(adresse.unerlaubteNutzungBis()).isEqualTo(adresseDTO.unerlaubteNutzungBis());
            assertThat(adresse.anzahlMahnungen()).isEqualTo(adresseDTO.anzahlMahnungen());
            assertThat(adresse.sondernutzungErlaubt()).isEqualTo(adresseDTO.sondernutzungErlaubt());
            assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(90);
        }
    }

}
