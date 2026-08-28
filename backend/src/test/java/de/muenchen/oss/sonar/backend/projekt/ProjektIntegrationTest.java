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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
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

    private UUID testProjektId;

    @BeforeEach
    public void setUp() {
        projektRepository.deleteAll();

        final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
        adresse.setBezeichnung("Marienplatz 8");
        adresse.setBaunutzung("Gastronomie");
        adresse.setUnerlaubteNutzungVon(BEGINN);
        adresse.setUnerlaubteNutzungBis(ENDE);
        adresse.setAnzahlMahnungen(0);
        adresse.setSondernutzungErlaubt(false);

        final ProjektEntity projekt = new ProjektEntity();
        projekt.setProjektnummer("2026-0001");
        projekt.setAbrechnungBeginn(BEGINN);
        projekt.setAbrechnungEnde(ENDE);
        projekt.addAdresse(adresse);

        testProjektId = projektRepository.save(projekt).getId();
    }

    @Nested
    class GetProjekt {
        @Test
        void givenProjektId_thenReturnProjekt() {
            restTestClient
                    .get()
                    .uri("/projekt/{projektId}", testProjektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.id()).isEqualTo(testProjektId);
                        assertThat(projektResponseDTO.adressen()).hasSize(1);
                        assertThat(projektResponseDTO.adressen().getFirst().bezeichnung()).isEqualTo("Marienplatz 8");
                    });
        }

        @Test
        void givenUnknownProjektId_thenReturnNotFound() {
            restTestClient
                    .get()
                    .uri("/projekt/{projektId}", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    class GetProjektePage {
        @Test
        void givenPageNumberAndPageSize_thenReturnPageOfProjekte() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageNumber", "0")
                            .queryParam("pageSize", "10")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.content")
                    .value(new ParameterizedTypeReference<List<ProjektResponseDTO>>() {
                    }, content -> assertThat(content.size()).isEqualTo(1));
        }

        @Test
        void givenNoSortParameters_thenPageByDescendingProjektnummerWithoutRepeatingOrSkipping() {
            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setBezeichnung("Marienplatz 8");
            adresse0003.setBaunutzung("Gastronomie");
            adresse0003.setUnerlaubteNutzungVon(BEGINN);
            adresse0003.setUnerlaubteNutzungBis(ENDE);
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setBezeichnung("Marienplatz 8");
            adresse0002.setBaunutzung("Gastronomie");
            adresse0002.setUnerlaubteNutzungVon(BEGINN);
            adresse0002.setUnerlaubteNutzungBis(ENDE);
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageNumber", "0")
                            .queryParam("pageSize", "2")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0003", "2026-0002"));

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "2")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0001"));
        }

        @Test
        void givenAscendingProjektnummer_thenReverseTheOrder() {
            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setBezeichnung("Marienplatz 8");
            adresse0003.setBaunutzung("Gastronomie");
            adresse0003.setUnerlaubteNutzungVon(BEGINN);
            adresse0003.setUnerlaubteNutzungBis(ENDE);
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setBezeichnung("Marienplatz 8");
            adresse0002.setBaunutzung("Gastronomie");
            adresse0002.setUnerlaubteNutzungVon(BEGINN);
            adresse0002.setUnerlaubteNutzungBis(ENDE);
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("sortBy", "PROJEKTNUMMER")
                            .queryParam("sortDirection", "ASC")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0001", "2026-0002", "2026-0003"));
        }

        @Test
        void givenAbrechnungBeginnAscending_thenOrderByThatColumn() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setBezeichnung("Marienplatz 8");
            adresse0002.setBaunutzung("Gastronomie");
            adresse0002.setUnerlaubteNutzungVon(LocalDate.of(2025, 1, 1));
            adresse0002.setUnerlaubteNutzungBis(ENDE);
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(LocalDate.of(2025, 1, 1));
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setBezeichnung("Marienplatz 8");
            adresse0003.setBaunutzung("Gastronomie");
            adresse0003.setUnerlaubteNutzungVon(LocalDate.of(2024, 1, 1));
            adresse0003.setUnerlaubteNutzungBis(ENDE);
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(LocalDate.of(2024, 1, 1));
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("sortBy", "ABRECHNUNG_BEGINN")
                            .queryParam("sortDirection", "ASC")
                            .queryParam("pageSize", "2")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0003", "2026-0002"));
        }

        @Test
        void givenUnknownSortBy_thenRejectTheRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("sortBy", "GEHEIMES_FELD")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenProjektnummerFragmentInDifferentCase_thenReturnMatchingProjekte() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(BEGINN);
            adresse.setUnerlaubteNutzungBis(ENDE);
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(BEGINN);
            projekt.setAbrechnungEnde(ENDE);
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("projektnummer", "27-47")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder("2027-4711"));
        }

        @Test
        void givenBlankProjektnummer_thenReturnAllProjekte() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(BEGINN);
            adresse.setUnerlaubteNutzungBis(ENDE);
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(BEGINN);
            projekt.setAbrechnungEnde(ENDE);
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("projektnummer", " ")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder("2026-0001", "2027-4711"));
        }

        @Test
        void givenAbrechnungBeginn_thenReturnOnlyProjekteStartingOnThatDay() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adresse.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(LocalDate.of(2027, 1, 1));
            projekt.setAbrechnungEnde(LocalDate.of(2027, 3, 31));
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("abrechnungBeginn", "2027-01-01")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder("2027-4711"));
        }

        @Test
        void givenAbrechnungEnde_thenReturnOnlyProjekteEndingOnThatDay() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adresse.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(LocalDate.of(2027, 1, 1));
            projekt.setAbrechnungEnde(LocalDate.of(2027, 3, 31));
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("abrechnungEnde", "2026-03-31")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder("2026-0001"));
        }

        @Test
        void givenDateOfNoProjekt_thenReturnNothing() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("abrechnungBeginn", "2026-01-02")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).isEmpty());
        }

        @Test
        void givenSeveralCriteria_thenCombineThemWithAnd() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adresse.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(LocalDate.of(2027, 1, 1));
            projekt.setAbrechnungEnde(LocalDate.of(2027, 3, 31));
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("projektnummer", "2027")
                            .queryParam("abrechnungBeginn", "2026-01-01")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).isEmpty());
        }

        @Test
        void givenFilter_thenTotalElementsCountsOnlyMatches() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setBezeichnung("Marienplatz 8");
            adresse.setBaunutzung("Gastronomie");
            adresse.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adresse.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2027-4711");
            projekt.setAbrechnungBeginn(LocalDate.of(2027, 1, 1));
            projekt.setAbrechnungEnde(LocalDate.of(2027, 3, 31));
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("projektnummer", "2027")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.page.totalElements").isEqualTo(1);
        }

        @Test
        void givenPageSizeBelowOne_thenReturnBadRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageSize", "0")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenPageSizeAboveMaximum_thenReturnBadRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageSize", "101")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenNegativePageNumber_thenReturnBadRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam("pageNumber", "-1")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
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
            transactionTemplate.executeWithoutResult(status -> {
                final ProjektEntity projekt = projektRepository.findById(responseDTO.id()).orElseThrow();
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

            transactionTemplate.executeWithoutResult(status -> {
                final ProjektEntity projekt = projektRepository.findById(responseDTO.id()).orElseThrow();
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
            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(responseDTO.id()).orElseThrow()
                            .getAdressen().getFirst().getTageUnerlaubteNutzung())
                    .isEqualTo(31));
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
