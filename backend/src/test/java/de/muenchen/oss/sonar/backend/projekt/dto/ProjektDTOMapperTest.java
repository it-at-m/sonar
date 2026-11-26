package de.muenchen.oss.sonar.backend.projekt.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
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
            final ProjektAdresse adresse = new ProjektAdresse(
                    UUID.randomUUID(), Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    BEGINN, ENDE, 90, 2, true);
            final Projekt projekt = new Projekt(
                    UUID.randomUUID(), "2026-0001", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(adresse), null, List.of(), List.of(), false, false);

            final ProjektResponseDTO result = projektDTOMapper.toDTO(projekt);

            assertNotNull(result);
            assertThat(result.id()).isEqualTo(projekt.id());
            assertThat(result.projektnummer()).isEqualTo(projekt.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(projekt.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(projekt.abrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresseResponseDTO adresseDTO = result.adressen().getFirst();
            assertThat(adresseDTO.id()).isEqualTo(adresse.id());
            assertThat(adresseDTO.art()).isEqualTo(adresse.art());
            assertThat(adresseDTO.adresse()).isEqualTo(adresse.adresse());
            assertThat(adresseDTO.hausnummerVon()).isEqualTo(adresse.hausnummerVon());
            assertThat(adresseDTO.hausnummerBis()).isEqualTo(adresse.hausnummerBis());
            assertThat(adresseDTO.flurstueck()).isEqualTo(adresse.flurstueck());
            assertThat(adresseDTO.gemarkung()).isEqualTo(adresse.gemarkung());
            assertThat(adresseDTO.nutzungen()).containsExactlyElementsOf(adresse.nutzungen());
            assertThat(adresseDTO.unerlaubteNutzungVon()).isEqualTo(adresse.unerlaubteNutzungVon());
            assertThat(adresseDTO.unerlaubteNutzungBis()).isEqualTo(adresse.unerlaubteNutzungBis());
            assertThat(adresseDTO.tageUnerlaubteNutzung()).isEqualTo(adresse.tageUnerlaubteNutzung());
            assertThat(adresseDTO.anzahlMahnungen()).isEqualTo(adresse.anzahlMahnungen());
            assertThat(adresseDTO.sondernutzungErlaubt()).isEqualTo(adresse.sondernutzungErlaubt());
        }

        @Test
        void givenLinkedProjekt_thenReturnsDTOWithItsPointerAndTheLinkedProjekte() {
            final UUID ersterId = UUID.randomUUID();
            final UUID zweiterId = UUID.randomUUID();
            final UUID vierterId = UUID.randomUUID();
            final Projekt projekt = new Projekt(UUID.randomUUID(), "2026-0003", BEGINN, ENDE, Projekt.VERWALTUNGSGEBUEHR, List.of(),
                    zweiterId, List.of(new LinkedProjekt(ersterId, "2026-0001"), new LinkedProjekt(zweiterId, "2026-0002")),
                    List.of(new LinkedProjekt(vierterId, "2026-0004")), false, false);

            final ProjektResponseDTO result = projektDTOMapper.toDTO(projekt);

            assertThat(result.vorgaengerProjektId()).isEqualTo(zweiterId);
            assertThat(result.vorgaengerProjekte()).containsExactly(
                    new LinkedProjektResponseDTO(ersterId, "2026-0001"),
                    new LinkedProjektResponseDTO(zweiterId, "2026-0002"));
            assertThat(result.nachfolgerProjekte()).containsExactly(new LinkedProjektResponseDTO(vierterId, "2026-0004"));
        }
    }

    @Nested
    class ToProjekt {
        @Test
        void givenRequestDTO_thenReturnsCorrectProjekt() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    BEGINN, ENDE, null, 1, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Projekt result = projektDTOMapper.toProjekt(requestDTO);

            assertNotNull(result);
            assertThat(result.id()).isNull();
            assertThat(result.projektnummer()).isEqualTo(requestDTO.projektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(requestDTO.abrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(requestDTO.abrechnungEnde());
            assertThat(result.verwaltungsgebuehr()).isEqualByComparingTo(Projekt.VERWALTUNGSGEBUEHR);
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresse adresse = result.adressen().getFirst();
            assertThat(adresse.id()).isNull();
            assertThat(adresse.art()).isEqualTo(adresseDTO.art());
            assertThat(adresse.adresse()).isEqualTo(adresseDTO.adresse());
            assertThat(adresse.hausnummerVon()).isEqualTo(adresseDTO.hausnummerVon());
            assertThat(adresse.hausnummerBis()).isEqualTo(adresseDTO.hausnummerBis());
            assertThat(adresse.flurstueck()).isEqualTo(adresseDTO.flurstueck());
            assertThat(adresse.gemarkung()).isEqualTo(adresseDTO.gemarkung());
            assertThat(adresse.nutzungen()).containsExactlyElementsOf(adresseDTO.nutzungen());
            assertThat(adresse.unerlaubteNutzungVon()).isEqualTo(adresseDTO.unerlaubteNutzungVon());
            assertThat(adresse.unerlaubteNutzungBis()).isEqualTo(adresseDTO.unerlaubteNutzungBis());
            assertThat(adresse.anzahlMahnungen()).isEqualTo(adresseDTO.anzahlMahnungen());
            assertThat(adresse.sondernutzungErlaubt()).isEqualTo(adresseDTO.sondernutzungErlaubt());
            assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(result.vorgaengerProjektId()).isNull();
            assertThat(result.vorgaengerProjekte()).isEmpty();
            assertThat(result.nachfolgerProjekte()).isEmpty();
        }
    }

}
