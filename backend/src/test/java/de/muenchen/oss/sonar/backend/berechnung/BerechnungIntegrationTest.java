package de.muenchen.oss.sonar.backend.berechnung;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungPositionEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.NutzungsobjektEntity;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungMastRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungNutzungsobjektRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungPositionRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungResponseDTO;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungResponseDTO;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektAdresseEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektRepository;
import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
@Import(TestSecurityConfiguration.class)
class BerechnungIntegrationTest {

    private static final LocalDate BEGINN = LocalDate.of(2025, 1, 6);
    private static final LocalDate ENDE = LocalDate.of(2025, 1, 26);
    private static final String BERECHNUNGSDATUM = "2025-12-31";

    private static final String ABRECHNUNG_PATH = "/projekt/{projektId}/abrechnung";
    private static final String UNSAVED_PREVIEW_PATH = "/projekt/{projektId}/abrechnung/berechnung/preview";
    private static final String STORED_PREVIEW_PATH = "/projekt/{projektId}/abrechnung/{abrechnungId}/berechnung/preview";

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AbrechnungRepository abrechnungRepository;

    @Autowired
    private ProjektRepository projektRepository;

    private UUID projektId;

    @BeforeEach
    public void setUp() {
        abrechnungRepository.deleteAll();
        projektRepository.deleteAll();

        final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
        adresse.setAnzahlMahnungen(0);
        adresse.setSondernutzungErlaubt(false);

        final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
        adressdaten.setArt(Adressart.ADRESSE);
        adressdaten.setAdresse("Marienplatz");
        adressdaten.setHausnummerVon("8");

        final ProjektEntity projekt = new ProjektEntity();
        projekt.setProjektnummer("2025-0001");
        projekt.setStadtbezirk(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL);
        projekt.setAbrechnungBeginn(BEGINN);
        projekt.setAbrechnungEnde(ENDE);
        projekt.addAdresse(adresse);

        projektId = projektRepository.save(projekt).getId();
    }

    @Nested
    class PreviewBerechnungOfUnsavedAbrechnung {

        @Test
        void givenNewNutzungsobjekt_thenReturnTheBerechnungAndStoreNothing() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(null,
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BerechnungResponseDTO.class)
                    .value(ergebnis -> {
                        assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("225");
                        assertThat(ergebnis.gebuehrVerwaltung()).isEqualByComparingTo("42");
                        assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("267");
                    });

            assertThat(abrechnungRepository.count()).isZero();
        }

        @Test
        void givenUebernommenesNutzungsobjekt_thenChargeWhatSavingStores() {
            final AbrechnungPositionEntity storedPosition = new AbrechnungPositionEntity();
            storedPosition.setBeginn(BEGINN);
            storedPosition.setEnde(ENDE);
            storedPosition.setLaenge(new BigDecimal("12.00"));
            storedPosition.setBreite(new BigDecimal("3.00"));
            storedPosition.setFlaeche(new BigDecimal("36.00"));

            final NutzungsobjektEntity storedNutzungsobjekt = new NutzungsobjektEntity();
            storedNutzungsobjekt.setAufschlag50prozent(true);
            storedNutzungsobjekt.addNutzung(Nutzung.BAUZAUN);

            final AdressdatenEmbeddable storedAdressdaten = storedNutzungsobjekt.getAdressdaten();
            storedAdressdaten.setArt(Adressart.ADRESSE);
            storedAdressdaten.setAdresse("Marienplatz");
            storedAdressdaten.setHausnummerVon("8");

            final AbrechnungEntity storedAbrechnung = new AbrechnungEntity();
            storedAbrechnung.setProjektId(projektId);
            storedAbrechnung.setVersionsnummer(1);
            storedAbrechnung.setGeschaeftspartnerId("1000000001");
            storedAbrechnung.setZeitraumVon(BEGINN);
            storedAbrechnung.setZeitraumBis(ENDE);
            storedAbrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            storedPosition.setNutzungsobjekt(storedNutzungsobjekt);
            storedAbrechnung.addNutzungsobjekt(storedNutzungsobjekt);
            storedAbrechnung.addPosition(storedPosition);
            final UUID nutzungsobjektId = abrechnungRepository.save(storedAbrechnung).getNutzungsobjekte().getFirst().getId();

            final AbrechnungNutzungsobjektRequestDTO uebernommenesNutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    nutzungsobjektId, Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, List.of(Nutzung.CONTAINER), null,
                    null, null, null, "Wird ignoriert", false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("2.00"),
                            new BigDecimal("20.00")),
                            new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("5.00"), new BigDecimal("2.00"),
                                    new BigDecimal("10.00"))),
                    List.of());
            final AbrechnungNutzungsobjektRequestDTO newNutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(null,
                    Adressart.ADRESSE, "Kaufingerstraße", "1", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(),
                    List.of(new AbrechnungMastRequestDTO(BEGINN, ENDE)));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000002", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(uebernommenesNutzungsobjekt, newNutzungsobjekt));

            final BerechnungResponseDTO beforeSaving = restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BerechnungResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            final AbrechnungResponseDTO saved = restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AbrechnungResponseDTO.class)
                    .returnResult()
                    .getResponseBody();
            assertThat(saved).isNotNull();

            final BerechnungResponseDTO afterSaving = restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(STORED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId, saved.id()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BerechnungResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(beforeSaving).isNotNull();
            assertThat(beforeSaving.gebuehrFlaechen()).isPositive();
            assertThat(beforeSaving.gebuehrUeberspannungen()).isPositive();
            assertThat(beforeSaving)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(afterSaving);
        }

        @Test
        void givenUnknownUebernommenesNutzungsobjekt_thenReturnNotFound() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(null,
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(UUID.randomUUID()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenProjektInStadtbezirkOfAnotherRoleAndUnknownNutzungsobjekt_thenReturnForbidden() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Nord")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        void givenInvertedZeitraum_thenReturnBadRequest() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(null,
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, ENDE, BEGINN,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(UNSAVED_PREVIEW_PATH)
                            .queryParam("berechnungsdatum", BERECHNUNGSDATUM)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenNoBerechnungsdatum_thenReturnBadRequest() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(null,
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), null,
                    null, null, null, null, false,
                    List.of(new AbrechnungPositionRequestDTO(BEGINN, ENDE, new BigDecimal("10.00"), new BigDecimal("5.00"),
                            new BigDecimal("50.00"))),
                    List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BEGINN, ENDE,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(UNSAVED_PREVIEW_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }
}
