package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
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
class ProjektIntegrationTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

    @Autowired
    private RestTestClient restTestClient;

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private ProjektRepository projektRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() {
        projektRepository.deleteAll();
        persistProjektWithOneAdresse("2026-0001", BEGINN, ENDE);
    }

    private void persistProjektWithOneAdresse(final String projektnummer, final LocalDate beginn, final LocalDate ende) {
        final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
        adresse.setBezeichnung("Marienplatz 8");
        adresse.setBaunutzung("Gastronomie");
        adresse.setUnerlaubteNutzungVon(beginn);
        adresse.setUnerlaubteNutzungBis(ende);
        adresse.setAnzahlMahnungen(0);
        adresse.setSondernutzungErlaubt(false);

        final ProjektEntity projekt = new ProjektEntity();
        projekt.setProjektnummer(projektnummer);
        projekt.setAbrechnungBeginn(beginn);
        projekt.setAbrechnungEnde(ende);
        projekt.addAdresse(adresse);

        projektRepository.save(projekt);
    }

    private void assertPersisted(final UUID projektId, final Consumer<ProjektEntity> assertions) {
        transactionTemplate.executeWithoutResult(
                status -> assertions.accept(projektRepository.findById(projektId).orElseThrow()));
    }

    @Nested
    class SaveProjekt {
        @Test
        void givenProjekt_thenProjektIsSaved() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 3, true);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0002", BEGINN, ENDE, List.of(adresseDTO));

            final ProjektResponseDTO responseDTO = restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.projektnummer()).isEqualTo(requestDTO.projektnummer());
                        assertThat(projektResponseDTO.adressen()).hasSize(1);
                    })
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();
            assertPersisted(responseDTO.id(), projekt -> {
                assertThat(projekt.getProjektnummer()).isEqualTo("2026-0002");
                assertThat(projekt.getAdressen()).hasSize(1);

                final ProjektAdresseEntity adresse = projekt.getAdressen().getFirst();
                assertThat(adresse.getBezeichnung()).isEqualTo("Flurstück 1234/5");
                assertThat(adresse.getBaunutzung()).isEqualTo("Wohnen");
                assertThat(adresse.getUnerlaubteNutzungVon()).isEqualTo(BEGINN);
                assertThat(adresse.getUnerlaubteNutzungBis()).isEqualTo(ENDE);
                assertThat(adresse.getAnzahlMahnungen()).isEqualTo(3);
                assertThat(adresse.isSondernutzungErlaubt()).isTrue();
            });
        }

        @Test
        void givenInvertedAbrechnungszeitraum_thenReturnBadRequest() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0005", ENDE, BEGINN, List.of(adresseDTO));

            restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(projektRepository.count()).isEqualTo(1);
        }

        @Test
        void givenTageInsteadOfZeitraum_thenProjektIsSavedWithThoseTage() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", null, null, 12, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0006", BEGINN, ENDE, List.of(adresseDTO));

            final ProjektResponseDTO responseDTO = restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProjektResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();
            assertThat(responseDTO.adressen().getFirst().tageUnerlaubteNutzung()).isEqualTo(12);

            assertPersisted(responseDTO.id(), projekt -> {
                final ProjektAdresseEntity persisted = projekt.getAdressen().getFirst();
                assertThat(persisted.getTageUnerlaubteNutzung()).isEqualTo(12);
                assertThat(persisted.getUnerlaubteNutzungVon()).isNull();
            });
        }

        @Test
        void givenZeitraum_thenTageAreDerivedFromIt() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0007", BEGINN, ENDE, List.of(adresseDTO));

            final ProjektResponseDTO responseDTO = restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> assertThat(
                            projektResponseDTO.adressen().getFirst().tageUnerlaubteNutzung()).isEqualTo(31))
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();
            assertPersisted(responseDTO.id(), projekt -> assertThat(
                    projekt.getAdressen().getFirst().getTageUnerlaubteNutzung()).isEqualTo(31));
        }

        @Test
        void givenZeitraumAndTage_thenReturnBadRequest() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, 12, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0008", BEGINN, ENDE, List.of(adresseDTO));

            restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(projektRepository.count()).isEqualTo(1);
        }

        @Test
        void givenProjektWithoutAdressen_thenReturnBadRequest() {
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0003", BEGINN, ENDE, List.of());

            restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(projektRepository.count()).isEqualTo(1);
        }
    }

}
