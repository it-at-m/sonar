package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungNutzungsobjektRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungPositionRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungResponseDTO;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektAdresseEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektRepository;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
@Import(TestSecurityConfiguration.class)
class AbrechnungIntegrationTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private static final String ABRECHNUNG_PATH = "/projekt/{projektId}/abrechnung";

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

    @Autowired
    private TransactionTemplate transactionTemplate;

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
        projekt.setProjektnummer("2026-0001");
        projekt.setAbrechnungBeginn(VON);
        projekt.setAbrechnungEnde(BIS);
        projekt.addAdresse(adresse);

        projektId = projektRepository.save(projekt).getId();
    }

    @Nested
    class SaveAbrechnung {
        @Test
        void givenAdresse_thenAbrechnungIsSaved() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung",
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            final AbrechnungResponseDTO responseDTO = restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(AbrechnungResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.projektId()).isEqualTo(projektId);
                        assertThat(dto.nutzungsobjekte()).hasSize(1);
                        assertThat(dto.nutzungsobjekte().getFirst().tageUnerlaubteNutzung()).isEqualTo(90);
                        assertThat(dto.nutzungsobjekte().getFirst().positionen().getFirst().flaeche())
                                .isEqualByComparingTo("36.00");
                    })
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();
            transactionTemplate.executeWithoutResult(status -> {
                final AbrechnungEntity abrechnung = abrechnungRepository.findById(responseDTO.id()).orElseThrow();
                assertThat(abrechnung.getGeschaeftspartnerId()).isEqualTo("1000000001");
                assertThat(abrechnung.getAbrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
                assertThat(abrechnung.getNutzungsobjekte()).hasSize(1);

                final AbrechnungNutzungsobjektEntity persisted = abrechnung.getNutzungsobjekte().getFirst();
                final AdressdatenEmbeddable persistedAdressdaten = persisted.getAdressdaten();
                assertThat(persistedAdressdaten.getArt()).isEqualTo(Adressart.ADRESSE);
                assertThat(persistedAdressdaten.getAdresse()).isEqualTo("Marienplatz");
                assertThat(persistedAdressdaten.getHausnummerVon()).isEqualTo("8");
                assertThat(persistedAdressdaten.getHausnummerBis()).isEqualTo("12");
                assertThat(persistedAdressdaten.getFlurstueck()).isNull();
                assertThat(persistedAdressdaten.getTageUnerlaubteNutzung()).isEqualTo(90);
                assertThat(persisted.getPositionen()).hasSize(1);
                assertThat(persisted.getPositionen().getFirst().getFlaeche()).isEqualByComparingTo("36.00");
                assertThat(persisted.getPositionen().getFirst().isHaelfte()).isTrue();
            });
        }

        @Test
        void givenFlurstueck_thenAbrechnungIsSaved() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", null,
                    null, null, 12, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AbrechnungResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.nutzungsobjekte().getFirst().flurstueck()).isEqualTo("1234/5");
                        assertThat(dto.nutzungsobjekte().getFirst().gemarkung()).isEqualTo("Sendling");
                        assertThat(dto.nutzungsobjekte().getFirst().tageUnerlaubteNutzung()).isEqualTo(12);
                    });

            assertThat(abrechnungRepository.count()).isEqualTo(1);
        }

        @Test
        void givenSeveralNutzungsobjekte_thenKeepTheOrderTheyWereEnteredIn() {
            final AbrechnungNutzungsobjektRequestDTO erste = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungNutzungsobjektRequestDTO zweite = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(erste, zweite));

            final AbrechnungResponseDTO responseDTO = restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AbrechnungResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();
            transactionTemplate.executeWithoutResult(status -> assertThat(
                    abrechnungRepository.findById(responseDTO.id()).orElseThrow().getNutzungsobjekte())
                    .extracting(nutzungsobjekt -> nutzungsobjekt.getAdressdaten().getAdresse())
                    .containsExactly("Marienplatz", "Sendlinger Straße"));
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_PATH, UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();

            assertThat(abrechnungRepository.count()).isZero();
        }

        @Test
        void givenAdresseWithGemarkung_thenReturnBadRequest() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, "Sendling", null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(abrechnungRepository.count()).isZero();
        }

        @Test
        void givenInvertedZeitraum_thenReturnBadRequest() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BIS, VON,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(abrechnungRepository.count()).isZero();
        }

        @Test
        void givenNoNutzungsobjekte_thenReturnBadRequest() {
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of());

            restTestClient.post()
                    .uri(ABRECHNUNG_PATH, projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(abrechnungRepository.count()).isZero();
        }
    }

}
