package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektAdresseView;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektModelMapper;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@AllArgsConstructor
class ProjektModelMapperTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private final ProjektModelMapper projektModelMapper = Mappers.getMapper(ProjektModelMapper.class);

    @Nested
    class ToView {
        @Test
        void givenEntity_thenReturnsCorrectView() {
            // Given
            final ProjektAdresse adresse = new ProjektAdresse();
            adresse.setId(UUID.randomUUID());
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(BEGINN);
            adresse.setUnerlaubteNutzungBis(ENDE);
            adresse.setAnzahlMahnungen(2);
            adresse.setSondernutzungErlaubt(true);

            final Projekt projekt = new Projekt();
            projekt.setId(UUID.randomUUID());
            projekt.setProjektnummer("2026-0001");
            projekt.setAbrechnungBeginn(BEGINN);
            projekt.setAbrechnungEnde(ENDE);
            projekt.addAdresse(adresse);

            // When
            final ProjektView result = projektModelMapper.toView(projekt);

            // Then
            assertNotNull(result);
            assertThat(result.id()).isEqualTo(projekt.getId());
            assertThat(result.projektnummer()).isEqualTo(projekt.getProjektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(projekt.getAbrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(projekt.getAbrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresseView adresseView = result.adressen().getFirst();
            assertThat(adresseView.id()).isEqualTo(adresse.getId());
            assertThat(adresseView.bezeichnung()).isEqualTo(adresse.getBezeichnung());
            assertThat(adresseView.baunutzung()).isEqualTo(adresse.getBaunutzung());
            assertThat(adresseView.unerlaubteNutzungVon()).isEqualTo(adresse.getUnerlaubteNutzungVon());
            assertThat(adresseView.unerlaubteNutzungBis()).isEqualTo(adresse.getUnerlaubteNutzungBis());
            assertThat(adresseView.anzahlMahnungen()).isEqualTo(adresse.getAnzahlMahnungen());
            assertThat(adresseView.sondernutzungErlaubt()).isEqualTo(adresse.isSondernutzungErlaubt());
        }
    }

    @Nested
    class ToEntity {
        @Test
        void givenCommand_thenReturnsCorrectEntity() {
            // Given
            final CreateProjektCommand.Adresse adresseCommand = new CreateProjektCommand.Adresse(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, false);
            final CreateProjektCommand command = new CreateProjektCommand("2026-0001", BEGINN, ENDE, List.of(adresseCommand));

            // When
            final Projekt result = projektModelMapper.toEntity(command);

            // Then
            assertNotNull(result);
            assertThat(result.getId()).isNull();
            assertThat(result.getProjektnummer()).isEqualTo(command.projektnummer());
            assertThat(result.getAbrechnungBeginn()).isEqualTo(command.abrechnungBeginn());
            assertThat(result.getAbrechnungEnde()).isEqualTo(command.abrechnungEnde());
            assertThat(result.getAdressen()).hasSize(1);

            final ProjektAdresse adresse = result.getAdressen().getFirst();
            assertThat(adresse.getId()).isNull();
            assertThat(adresse.getBezeichnung()).isEqualTo(adresseCommand.bezeichnung());
            assertThat(adresse.getBaunutzung()).isEqualTo(adresseCommand.baunutzung());
            assertThat(adresse.getUnerlaubteNutzungVon()).isEqualTo(adresseCommand.unerlaubteNutzungVon());
            assertThat(adresse.getUnerlaubteNutzungBis()).isEqualTo(adresseCommand.unerlaubteNutzungBis());
            assertThat(adresse.getAnzahlMahnungen()).isEqualTo(adresseCommand.anzahlMahnungen());
            assertThat(adresse.isSondernutzungErlaubt()).isEqualTo(adresseCommand.sondernutzungErlaubt());
        }

        @Test
        void givenZeitraum_thenDeriveTageUnerlaubteNutzung() {
            // Given: 01.01.2026 until 31.03.2026, both boundaries counted
            final CreateProjektCommand.Adresse adresseCommand = new CreateProjektCommand.Adresse(
                    "Marienplatz 8", null, BEGINN, ENDE, null, 0, false);
            final CreateProjektCommand command = new CreateProjektCommand("2026-0001", BEGINN, ENDE, List.of(adresseCommand));

            // When
            final Projekt result = projektModelMapper.toEntity(command);

            // Then
            assertThat(result.getAdressen().getFirst().getTageUnerlaubteNutzung()).isEqualTo(90);
        }

        @Test
        void givenOnlyTage_thenKeepThemInsteadOfDeriving() {
            // Given
            final CreateProjektCommand.Adresse adresseCommand = new CreateProjektCommand.Adresse(
                    "Marienplatz 8", null, null, null, 12, 0, false);
            final CreateProjektCommand command = new CreateProjektCommand("2026-0001", BEGINN, ENDE, List.of(adresseCommand));

            // When
            final Projekt result = projektModelMapper.toEntity(command);

            // Then
            final ProjektAdresse adresse = result.getAdressen().getFirst();
            assertThat(adresse.getTageUnerlaubteNutzung()).isEqualTo(12);
            assertThat(adresse.getUnerlaubteNutzungVon()).isNull();
            assertThat(adresse.getUnerlaubteNutzungBis()).isNull();
        }
    }

}
