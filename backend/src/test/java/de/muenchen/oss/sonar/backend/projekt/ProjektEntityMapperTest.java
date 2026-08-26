package de.muenchen.oss.sonar.backend.projekt;

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
class ProjektEntityMapperTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    private final ProjektEntityMapper projektEntityMapper = Mappers.getMapper(ProjektEntityMapper.class);

    @Nested
    class ToProjekt {
        @Test
        void givenEntity_thenReturnsCorrectProjekt() {
            // Given
            final ProjektAdresseEntity adresseEntity = new ProjektAdresseEntity();
            adresseEntity.setId(UUID.randomUUID());
            adresseEntity.setBezeichnung("Marienplatz 8");
            adresseEntity.setBaunutzung("Gastronomie");
            adresseEntity.setUnerlaubteNutzungVon(BEGINN);
            adresseEntity.setUnerlaubteNutzungBis(ENDE);
            adresseEntity.setTageUnerlaubteNutzung(90);
            adresseEntity.setAnzahlMahnungen(2);
            adresseEntity.setSondernutzungErlaubt(true);

            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(UUID.randomUUID());
            projektEntity.setProjektnummer("2026-0001");
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);
            projektEntity.addAdresse(adresseEntity);

            // When
            final Projekt result = projektEntityMapper.toProjekt(projektEntity);

            // Then
            assertNotNull(result);
            assertThat(result.id()).isEqualTo(projektEntity.getId());
            assertThat(result.projektnummer()).isEqualTo(projektEntity.getProjektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(projektEntity.getAbrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(projektEntity.getAbrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresse adresse = result.adressen().getFirst();
            assertThat(adresse.id()).isEqualTo(adresseEntity.getId());
            assertThat(adresse.bezeichnung()).isEqualTo(adresseEntity.getBezeichnung());
            assertThat(adresse.baunutzung()).isEqualTo(adresseEntity.getBaunutzung());
            assertThat(adresse.unerlaubteNutzungVon()).isEqualTo(adresseEntity.getUnerlaubteNutzungVon());
            assertThat(adresse.unerlaubteNutzungBis()).isEqualTo(adresseEntity.getUnerlaubteNutzungBis());
            assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(adresseEntity.getTageUnerlaubteNutzung());
            assertThat(adresse.anzahlMahnungen()).isEqualTo(adresseEntity.getAnzahlMahnungen());
            assertThat(adresse.sondernutzungErlaubt()).isEqualTo(adresseEntity.isSondernutzungErlaubt());
        }
    }

    @Nested
    class ToEntity {
        @Test
        void givenProjekt_thenReturnsCorrectEntity() {
            // Given
            final ProjektAdresse adresse = new ProjektAdresse(
                    null, "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 1, false);
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, List.of(adresse));

            // When
            final ProjektEntity result = projektEntityMapper.toEntity(projekt);

            // Then
            assertNotNull(result);
            assertThat(result.getProjektnummer()).isEqualTo(projekt.projektnummer());
            assertThat(result.getAbrechnungBeginn()).isEqualTo(projekt.abrechnungBeginn());
            assertThat(result.getAbrechnungEnde()).isEqualTo(projekt.abrechnungEnde());
            assertThat(result.getAdressen()).hasSize(1);

            final ProjektAdresseEntity adresseEntity = result.getAdressen().getFirst();
            assertThat(adresseEntity.getBezeichnung()).isEqualTo(adresse.bezeichnung());
            assertThat(adresseEntity.getBaunutzung()).isEqualTo(adresse.baunutzung());
            assertThat(adresseEntity.getUnerlaubteNutzungVon()).isEqualTo(adresse.unerlaubteNutzungVon());
            assertThat(adresseEntity.getUnerlaubteNutzungBis()).isEqualTo(adresse.unerlaubteNutzungBis());
            assertThat(adresseEntity.getTageUnerlaubteNutzung()).isEqualTo(adresse.tageUnerlaubteNutzung());
            assertThat(adresseEntity.getAnzahlMahnungen()).isEqualTo(adresse.anzahlMahnungen());
            assertThat(adresseEntity.isSondernutzungErlaubt()).isEqualTo(adresse.sondernutzungErlaubt());
        }

        @Test
        void givenProjektWithId_thenIdIsNotCarriedOver() {
            // Given
            final ProjektAdresse adresse = new ProjektAdresse(
                    UUID.randomUUID(), "Marienplatz 8", null, null, null, 12, 0, false);
            final Projekt projekt = new Projekt(UUID.randomUUID(), "2026-0001", BEGINN, ENDE, List.of(adresse));

            // When
            final ProjektEntity result = projektEntityMapper.toEntity(projekt);

            // Then
            assertThat(result.getId()).isNull();
            assertThat(result.getAdressen().getFirst().getId()).isNull();
        }
    }

}
