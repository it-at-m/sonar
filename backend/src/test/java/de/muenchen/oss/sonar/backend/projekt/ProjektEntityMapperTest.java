package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
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
            final ProjektAdresseEntity adresseEntity = new ProjektAdresseEntity();
            adresseEntity.setId(UUID.randomUUID());
            adresseEntity.setAnzahlMahnungen(2);
            adresseEntity.setSondernutzungErlaubt(true);

            final AdressdatenEmbeddable adressdaten = adresseEntity.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);
            adressdaten.setUnerlaubteNutzungVon(BEGINN);
            adressdaten.setUnerlaubteNutzungBis(ENDE);
            adressdaten.setTageUnerlaubteNutzung(90);

            final ProjektEntity projektEntity = new ProjektEntity();
            projektEntity.setId(UUID.randomUUID());
            projektEntity.setProjektnummer("2026-0001");
            projektEntity.setAbrechnungBeginn(BEGINN);
            projektEntity.setAbrechnungEnde(ENDE);
            projektEntity.addAdresse(adresseEntity);

            final Projekt result = projektEntityMapper.toProjekt(projektEntity);

            assertNotNull(result);
            assertThat(result.id()).isEqualTo(projektEntity.getId());
            assertThat(result.projektnummer()).isEqualTo(projektEntity.getProjektnummer());
            assertThat(result.abrechnungBeginn()).isEqualTo(projektEntity.getAbrechnungBeginn());
            assertThat(result.abrechnungEnde()).isEqualTo(projektEntity.getAbrechnungEnde());
            assertThat(result.adressen()).hasSize(1);

            final ProjektAdresse adresse = result.adressen().getFirst();
            assertThat(adresse.id()).isEqualTo(adresseEntity.getId());
            assertThat(adresse.art()).isEqualTo(adressdaten.getArt());
            assertThat(adresse.adresse()).isEqualTo(adressdaten.getAdresse());
            assertThat(adresse.hausnummerVon()).isEqualTo(adressdaten.getHausnummerVon());
            assertThat(adresse.hausnummerBis()).isEqualTo(adressdaten.getHausnummerBis());
            assertThat(adresse.flurstueck()).isEqualTo(adressdaten.getFlurstueck());
            assertThat(adresse.gemarkung()).isEqualTo(adressdaten.getGemarkung());
            assertThat(adresse.nutzung()).isEqualTo(adressdaten.getNutzung());
            assertThat(adresse.unerlaubteNutzungVon()).isEqualTo(adressdaten.getUnerlaubteNutzungVon());
            assertThat(adresse.unerlaubteNutzungBis()).isEqualTo(adressdaten.getUnerlaubteNutzungBis());
            assertThat(adresse.tageUnerlaubteNutzung()).isEqualTo(adressdaten.getTageUnerlaubteNutzung());
            assertThat(adresse.anzahlMahnungen()).isEqualTo(adresseEntity.getAnzahlMahnungen());
            assertThat(adresse.sondernutzungErlaubt()).isEqualTo(adresseEntity.isSondernutzungErlaubt());
        }
    }

    @Nested
    class ToEntity {
        @Test
        void givenProjekt_thenReturnsCorrectEntity() {
            final ProjektAdresse adresse = new ProjektAdresse(
                    null, Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", Nutzung.NUTZUNG_B,
                    BEGINN, ENDE, null, 1, false);
            final Projekt projekt = new Projekt(null, "2026-0001", BEGINN, ENDE, List.of(adresse));

            final ProjektEntity result = projektEntityMapper.toEntity(projekt);

            assertNotNull(result);
            assertThat(result.getProjektnummer()).isEqualTo(projekt.projektnummer());
            assertThat(result.getAbrechnungBeginn()).isEqualTo(projekt.abrechnungBeginn());
            assertThat(result.getAbrechnungEnde()).isEqualTo(projekt.abrechnungEnde());
            assertThat(result.getAdressen()).hasSize(1);

            final ProjektAdresseEntity adresseEntity = result.getAdressen().getFirst();
            final AdressdatenEmbeddable adressdaten = adresseEntity.getAdressdaten();
            assertThat(adressdaten.getArt()).isEqualTo(adresse.art());
            assertThat(adressdaten.getAdresse()).isEqualTo(adresse.adresse());
            assertThat(adressdaten.getHausnummerVon()).isEqualTo(adresse.hausnummerVon());
            assertThat(adressdaten.getHausnummerBis()).isEqualTo(adresse.hausnummerBis());
            assertThat(adressdaten.getFlurstueck()).isEqualTo(adresse.flurstueck());
            assertThat(adressdaten.getGemarkung()).isEqualTo(adresse.gemarkung());
            assertThat(adressdaten.getNutzung()).isEqualTo(adresse.nutzung());
            assertThat(adressdaten.getUnerlaubteNutzungVon()).isEqualTo(adresse.unerlaubteNutzungVon());
            assertThat(adressdaten.getUnerlaubteNutzungBis()).isEqualTo(adresse.unerlaubteNutzungBis());
            assertThat(adressdaten.getTageUnerlaubteNutzung()).isEqualTo(adresse.tageUnerlaubteNutzung());
            assertThat(adresseEntity.getAnzahlMahnungen()).isEqualTo(adresse.anzahlMahnungen());
            assertThat(adresseEntity.isSondernutzungErlaubt()).isEqualTo(adresse.sondernutzungErlaubt());
        }

        @Test
        void givenProjektWithId_thenIdIsNotCarriedOver() {
            final ProjektAdresse adresse = new ProjektAdresse(
                    UUID.randomUUID(), Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, 12, 0, false);
            final Projekt projekt = new Projekt(UUID.randomUUID(), "2026-0001", BEGINN, ENDE, List.of(adresse));

            final ProjektEntity result = projektEntityMapper.toEntity(projekt);

            assertThat(result.getId()).isNull();
            assertThat(result.getAdressen().getFirst().getId()).isNull();
        }
    }

}
