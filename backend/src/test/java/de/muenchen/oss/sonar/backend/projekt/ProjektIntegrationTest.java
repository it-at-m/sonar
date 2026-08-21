package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.springframework.web.util.UriBuilder;
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
        testProjektId = persistProjektWithOneAdresse("2026-0001", BEGINN, ENDE);
    }

    private UUID persistProjektWithOneAdresse(final String projektnummer, final LocalDate beginn, final LocalDate ende) {
        final ProjektAdresse adresse = new ProjektAdresse();
        adresse.setBezeichnung("Marienplatz 8");
        adresse.setBaunutzung("Gastronomie");
        adresse.setUnerlaubteNutzungVon(beginn);
        adresse.setUnerlaubteNutzungBis(ende);
        adresse.setAnzahlMahnungen(0);
        adresse.setSondernutzungErlaubt(false);

        final Projekt projekt = new Projekt();
        projekt.setProjektnummer(projektnummer);
        projekt.setAbrechnungBeginn(beginn);
        projekt.setAbrechnungEnde(ende);
        projekt.addAdresse(adresse);

        return projektRepository.save(projekt).getId();
    }

    private void assertPersisted(final UUID projektId, final Consumer<Projekt> assertions) {
        transactionTemplate.executeWithoutResult(
                status -> assertions.accept(projektRepository.findById(projektId).orElseThrow()));
    }

    private void expectProjektnummernInAnyOrder(final Function<UriBuilder, URI> uriFunction, final String... expectedProjektnummern) {
        requestProjektnummern(uriFunction, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder(expectedProjektnummern));
    }

    private void expectProjektnummernInOrder(final Function<UriBuilder, URI> uriFunction, final String... expectedProjektnummern) {
        requestProjektnummern(uriFunction, projektnummern -> assertThat(projektnummern).containsExactly(expectedProjektnummern));
    }

    private void requestProjektnummern(final Function<UriBuilder, URI> uriFunction, final Consumer<List<String>> assertions) {
        restTestClient.get()
                .uri(uriFunction)
                .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content..projektnummer")
                .value(new ParameterizedTypeReference<List<String>>() {
                }, assertions);
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

        /**
         * The two additional Projekte are inserted out of order on purpose: without an ORDER BY the
         * database would be free to hand back the rows in insertion order, which would let this
         * pass by accident.
         */
        @Test
        void givenNoSortParameters_thenPageByDescendingProjektnummerWithoutRepeatingOrSkipping() {
            persistProjektWithOneAdresse("2026-0003", BEGINN, ENDE);
            persistProjektWithOneAdresse("2026-0002", BEGINN, ENDE);

            expectProjektnummernInOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("pageNumber", "0")
                    .queryParam("pageSize", "2")
                    .build(), "2026-0003", "2026-0002");

            expectProjektnummernInOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("pageNumber", "1")
                    .queryParam("pageSize", "2")
                    .build(), "2026-0001");
        }

        @Test
        void givenAscendingProjektnummer_thenReverseTheOrder() {
            persistProjektWithOneAdresse("2026-0003", BEGINN, ENDE);
            persistProjektWithOneAdresse("2026-0002", BEGINN, ENDE);

            expectProjektnummernInOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("sortBy", "PROJEKTNUMMER")
                    .queryParam("sortDirection", "ASC")
                    .build(), "2026-0001", "2026-0002", "2026-0003");
        }

        /**
         * The Abrechnung Beginn order differs from the Projektnummer order here, so ordering by
         * another column is not satisfied by the default one.
         */
        @Test
        void givenAbrechnungBeginnAscending_thenOrderByThatColumn() {
            persistProjektWithOneAdresse("2026-0002", LocalDate.of(2025, 1, 1), ENDE);
            persistProjektWithOneAdresse("2026-0003", LocalDate.of(2024, 1, 1), ENDE);

            expectProjektnummernInOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("sortBy", "ABRECHNUNG_BEGINN")
                    .queryParam("sortDirection", "ASC")
                    .queryParam("pageSize", "2")
                    .build(), "2026-0003", "2026-0002");
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
            persistProjektWithOneAdresse("2027-4711", BEGINN, ENDE);

            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("projektnummer", "27-47")
                    .build(), "2027-4711");
        }

        @Test
        void givenBlankProjektnummer_thenReturnAllProjekte() {
            persistProjektWithOneAdresse("2027-4711", BEGINN, ENDE);

            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("projektnummer", " ")
                    .build(), "2026-0001", "2027-4711");
        }

        @Test
        void givenAbrechnungBeginn_thenReturnOnlyProjekteStartingOnThatDay() {
            persistProjektWithOneAdresse("2027-4711", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));

            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("abrechnungBeginn", "2027-01-01")
                    .build(), "2027-4711");
        }

        @Test
        void givenAbrechnungEnde_thenReturnOnlyProjekteEndingOnThatDay() {
            persistProjektWithOneAdresse("2027-4711", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));

            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("abrechnungEnde", "2026-03-31")
                    .build(), "2026-0001");
        }

        @Test
        void givenDateOfNoProjekt_thenReturnNothing() {
            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("abrechnungBeginn", "2026-01-02")
                    .build());
        }

        @Test
        void givenSeveralCriteria_thenCombineThemWithAnd() {
            persistProjektWithOneAdresse("2027-4711", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));

            expectProjektnummernInAnyOrder(uriBuilder -> uriBuilder
                    .path("/projekt")
                    .queryParam("projektnummer", "2027")
                    .queryParam("abrechnungBeginn", "2026-01-01")
                    .build());
        }

        @Test
        void givenFilter_thenTotalElementsCountsOnlyMatches() {
            persistProjektWithOneAdresse("2027-4711", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));

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
            expectBadRequest("pageSize", "0");
        }

        @Test
        void givenPageSizeAboveMaximum_thenReturnBadRequest() {
            expectBadRequest("pageSize", "101");
        }

        @Test
        void givenNegativePageNumber_thenReturnBadRequest() {
            expectBadRequest("pageNumber", "-1");
        }

        private void expectBadRequest(final String parameter, final String value) {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/projekt")
                            .queryParam(parameter, value)
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
            assertPersisted(responseDTO.id(), projekt -> {
                assertThat(projekt.getProjektnummer()).isEqualTo("2026-0002");
                assertThat(projekt.getAdressen()).hasSize(1);

                final ProjektAdresse adresse = projekt.getAdressen().getFirst();
                assertThat(adresse.getBezeichnung()).isEqualTo("Flurstück 1234/5");
                assertThat(adresse.getBaunutzung()).isEqualTo("Wohnen");
                assertThat(adresse.getUnerlaubteNutzungVon()).isEqualTo(BEGINN);
                assertThat(adresse.getUnerlaubteNutzungBis()).isEqualTo(ENDE);
                assertThat(adresse.getAnzahlMahnungen()).isEqualTo(3);
                assertThat(adresse.isSondernutzungErlaubt()).isTrue();
            });
        }

        @Test
        void givenReaderRole_thenReturnForbidden() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", BEGINN, ENDE, null, 3, true);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0004", BEGINN, ENDE, List.of(adresseDTO));

            restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isForbidden();

            assertThat(projektRepository.count()).isEqualTo(1);
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
                final ProjektAdresse persisted = projekt.getAdressen().getFirst();
                assertThat(persisted.getTageUnerlaubteNutzung()).isEqualTo(12);
                assertThat(persisted.getUnerlaubteNutzungVon()).isNull();
            });
        }

        @Test
        void givenZeitraum_thenTageAreDerivedFromIt() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    "Flurstück 1234/5", "Wohnen", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0007", BEGINN, ENDE, List.of(adresseDTO));

            restTestClient.post()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> assertThat(
                            projektResponseDTO.adressen().getFirst().tageUnerlaubteNutzung()).isEqualTo(31));
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
