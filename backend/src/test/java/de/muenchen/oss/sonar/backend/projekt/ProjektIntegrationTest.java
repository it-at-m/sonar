package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.dto.LinkedProjektResponseDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektAdresseResponseDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.VorgaengerProjektRequestDTO;
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
import org.springframework.http.HttpStatus;
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

    private UUID projektId;

    @BeforeEach
    public void setUp() {
        transactionTemplate.executeWithoutResult(status -> projektRepository.findAll()
                .forEach(projekt -> projekt.setVorgaengerProjektId(null)));
        projektRepository.deleteAll();

        final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
        adresse.setAnzahlMahnungen(0);
        adresse.setSondernutzungErlaubt(false);

        final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
        adressdaten.setArt(Adressart.ADRESSE);
        adressdaten.setAdresse("Marienplatz");
        adressdaten.setHausnummerVon("8");
        adresse.addNutzung(Nutzung.BAUZAUN);
        adressdaten.setUnerlaubteNutzungVon(BEGINN);
        adressdaten.setUnerlaubteNutzungBis(ENDE);

        final ProjektEntity projekt = new ProjektEntity();
        projekt.setProjektnummer("2026-0001");
        projekt.setAbrechnungBeginn(BEGINN);
        projekt.setAbrechnungEnde(ENDE);
        projekt.addAdresse(adresse);

        projektId = projektRepository.save(projekt).getId();
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
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

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
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

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
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(LocalDate.of(2025, 1, 1));
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(LocalDate.of(2025, 1, 1));
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(LocalDate.of(2024, 1, 1));
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(BEGINN);
            adressdaten.setUnerlaubteNutzungBis(ENDE);

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(BEGINN);
            adressdaten.setUnerlaubteNutzungBis(ENDE);

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adressdaten.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adressdaten.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adressdaten.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));

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
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);
            adressdaten.setUnerlaubteNutzungVon(LocalDate.of(2027, 1, 1));
            adressdaten.setUnerlaubteNutzungBis(LocalDate.of(2027, 3, 31));

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
        void givenStandaloneProjekteSharingAnAdresse_thenEachCanGetTheOtherAsVorgaenger() {
            final ProjektAdresseEntity gemeinsameAdresse = new ProjektAdresseEntity();
            gemeinsameAdresse.setAnzahlMahnungen(0);
            gemeinsameAdresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable gemeinsameAdressdaten = gemeinsameAdresse.getAdressdaten();
            gemeinsameAdressdaten.setArt(Adressart.ADRESSE);
            gemeinsameAdressdaten.setAdresse("Marienplatz");
            gemeinsameAdressdaten.setHausnummerVon("8");
            gemeinsameAdresse.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektMitGemeinsamerAdresse = new ProjektEntity();
            projektMitGemeinsamerAdresse.setProjektnummer("2026-0002");
            projektMitGemeinsamerAdresse.setAbrechnungBeginn(BEGINN);
            projektMitGemeinsamerAdresse.setAbrechnungEnde(ENDE);
            projektMitGemeinsamerAdresse.addAdresse(gemeinsameAdresse);
            projektRepository.save(projektMitGemeinsamerAdresse);

            final ProjektAdresseEntity eigeneAdresse = new ProjektAdresseEntity();
            eigeneAdresse.setAnzahlMahnungen(0);
            eigeneAdresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable eigeneAdressdaten = eigeneAdresse.getAdressdaten();
            eigeneAdressdaten.setArt(Adressart.ADRESSE);
            eigeneAdressdaten.setAdresse("Sendlinger Straße");
            eigeneAdressdaten.setHausnummerVon("1");
            eigeneAdresse.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektMitEigenerAdresse = new ProjektEntity();
            projektMitEigenerAdresse.setProjektnummer("2026-0003");
            projektMitEigenerAdresse.setAbrechnungBeginn(BEGINN);
            projektMitEigenerAdresse.setAbrechnungEnde(ENDE);
            projektMitEigenerAdresse.addAdresse(eigeneAdresse);
            projektRepository.save(projektMitEigenerAdresse);

            restTestClient.get()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[?(@.verknuepfungMoeglich == true)].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactlyInAnyOrder("2026-0001", "2026-0002"));
        }

        @Test
        void givenAdresseSharedOnlyWithAProjektWithNachfolger_thenNoVorgaengerIsPossible() {
            final ProjektAdresseEntity adresseNachfolger = new ProjektAdresseEntity();
            adresseNachfolger.setAnzahlMahnungen(0);
            adresseNachfolger.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdatenNachfolger = adresseNachfolger.getAdressdaten();
            adressdatenNachfolger.setArt(Adressart.ADRESSE);
            adressdatenNachfolger.setAdresse("Sendlinger Straße");
            adressdatenNachfolger.setHausnummerVon("1");
            adresseNachfolger.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity nachfolger = new ProjektEntity();
            nachfolger.setProjektnummer("2026-0002");
            nachfolger.setAbrechnungBeginn(BEGINN);
            nachfolger.setAbrechnungEnde(ENDE);
            nachfolger.setVorgaengerProjektId(projektId);
            nachfolger.addAdresse(adresseNachfolger);
            projektRepository.save(nachfolger);

            final ProjektAdresseEntity adresseWieVorgaenger = new ProjektAdresseEntity();
            adresseWieVorgaenger.setAnzahlMahnungen(0);
            adresseWieVorgaenger.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdatenWieVorgaenger = adresseWieVorgaenger.getAdressdaten();
            adressdatenWieVorgaenger.setArt(Adressart.ADRESSE);
            adressdatenWieVorgaenger.setAdresse("Marienplatz");
            adressdatenWieVorgaenger.setHausnummerVon("8");
            adresseWieVorgaenger.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektWieVorgaenger = new ProjektEntity();
            projektWieVorgaenger.setProjektnummer("2026-0003");
            projektWieVorgaenger.setAbrechnungBeginn(BEGINN);
            projektWieVorgaenger.setAbrechnungEnde(ENDE);
            projektWieVorgaenger.addAdresse(adresseWieVorgaenger);
            projektRepository.save(projektWieVorgaenger);

            restTestClient.get()
                    .uri("/projekt")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[?(@.verknuepfungMoeglich == true)].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).isEmpty());
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

        @Test
        void givenLinkedProjekte_thenEachProjektComesWithItsVorgaengerAndItsNachfolgerInTheirOrder() {
            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.setVorgaengerProjektId(projektId);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projekt0003Id);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0000 = new ProjektAdresseEntity();
            adresse0000.setAnzahlMahnungen(0);
            adresse0000.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0000 = adresse0000.getAdressdaten();
            adressdaten0000.setArt(Adressart.ADRESSE);
            adressdaten0000.setAdresse("Marienplatz");
            adressdaten0000.setHausnummerVon("8");
            adresse0000.addNutzung(Nutzung.BAUZAUN);
            adressdaten0000.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0000.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0000 = new ProjektEntity();
            projekt0000.setProjektnummer("2026-0000");
            projekt0000.setAbrechnungBeginn(BEGINN);
            projekt0000.setAbrechnungEnde(ENDE);
            projekt0000.addAdresse(adresse0000);
            projektRepository.save(projekt0000);

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
                    .jsonPath("$.content")
                    .value(new ParameterizedTypeReference<List<ProjektResponseDTO>>() {
                    }, content -> {
                        assertThat(content).extracting(ProjektResponseDTO::projektnummer)
                                .containsExactly("2026-0000", "2026-0001", "2026-0002", "2026-0003");
                        assertThat(content.get(0).vorgaengerProjekte()).isEmpty();
                        assertThat(content.get(0).nachfolgerProjekte()).isEmpty();
                        assertThat(content.get(1).vorgaengerProjekte()).isEmpty();
                        assertThat(content.get(1).nachfolgerProjekte()).extracting(LinkedProjektResponseDTO::projektnummer)
                                .containsExactly("2026-0003", "2026-0002");
                        assertThat(content.get(2).vorgaengerProjekte()).extracting(LinkedProjektResponseDTO::projektnummer)
                                .containsExactly("2026-0001", "2026-0003");
                        assertThat(content.get(2).nachfolgerProjekte()).isEmpty();
                        assertThat(content.get(3).vorgaengerProjekte()).extracting(LinkedProjektResponseDTO::projektnummer)
                                .containsExactly("2026-0001");
                        assertThat(content.get(3).nachfolgerProjekte()).extracting(LinkedProjektResponseDTO::projektnummer)
                                .containsExactly("2026-0002");
                    });
        }
    }

    @Nested
    class GetProjekt {
        @Test
        void givenKnownId_thenReturnProjektWithItsAdressen() {
            restTestClient.get()
                    .uri("/projekt/{projektId}", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.id()).isEqualTo(projektId);
                        assertThat(projektResponseDTO.projektnummer()).isEqualTo("2026-0001");
                        assertThat(projektResponseDTO.adressen()).hasSize(1);

                        final ProjektAdresseResponseDTO adresse = projektResponseDTO.adressen().getFirst();
                        assertThat(adresse.art()).isEqualTo(Adressart.ADRESSE);
                        assertThat(adresse.adresse()).isEqualTo("Marienplatz");
                        assertThat(adresse.hausnummerVon()).isEqualTo("8");
                        assertThat(adresse.nutzungen()).containsExactly(Nutzung.BAUZAUN);
                    });
        }

        @Test
        void givenStandaloneProjekt_thenReturnNoLinkedProjekte() {
            restTestClient.get()
                    .uri("/projekt/{projektId}", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.vorgaengerProjekte").isArray()
                    .jsonPath("$.vorgaengerProjekte").isEmpty()
                    .jsonPath("$.nachfolgerProjekte").isArray()
                    .jsonPath("$.nachfolgerProjekte").isEmpty();
        }

        @Test
        void givenProjektWithVorgaengerAndNachfolger_thenReturnThemInTheirOrder() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.setVorgaengerProjektId(projekt0002Id);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            final ProjektAdresseEntity adresse0004 = new ProjektAdresseEntity();
            adresse0004.setAnzahlMahnungen(0);
            adresse0004.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0004 = adresse0004.getAdressdaten();
            adressdaten0004.setArt(Adressart.ADRESSE);
            adressdaten0004.setAdresse("Marienplatz");
            adressdaten0004.setHausnummerVon("8");
            adresse0004.addNutzung(Nutzung.BAUZAUN);
            adressdaten0004.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0004.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0004 = new ProjektEntity();
            projekt0004.setProjektnummer("2026-0004");
            projekt0004.setAbrechnungBeginn(BEGINN);
            projekt0004.setAbrechnungEnde(ENDE);
            projekt0004.setVorgaengerProjektId(projekt0003Id);
            projekt0004.addAdresse(adresse0004);
            final UUID projekt0004Id = projektRepository.save(projekt0004).getId();

            restTestClient.get()
                    .uri("/projekt/{projektId}", projekt0003Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.vorgaengerProjektId()).isEqualTo(projekt0002Id);
                        assertThat(projektResponseDTO.vorgaengerProjekte()).containsExactly(
                                new LinkedProjektResponseDTO(projektId, "2026-0001"),
                                new LinkedProjektResponseDTO(projekt0002Id, "2026-0002"));
                        assertThat(projektResponseDTO.nachfolgerProjekte())
                                .containsExactly(new LinkedProjektResponseDTO(projekt0004Id, "2026-0004"));
                    });
        }

        @Test
        void givenUnknownId_thenReturnNotFound() {
            restTestClient.get()
                    .uri("/projekt/{projektId}", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenIdThatIsNoUuid_thenReturnBadRequest() {
            restTestClient.get()
                    .uri("/projekt/{projektId}", "2026-0001")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    class GetVorgaengerVorschlaege {
        @Test
        void givenProjekteWithOneOfItsAdressen_thenReturnThemWithoutTheProjektItself() {
            final ProjektAdresseEntity andereAdresse = new ProjektAdresseEntity();
            andereAdresse.setAnzahlMahnungen(0);
            andereAdresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable andereAdressdaten = andereAdresse.getAdressdaten();
            andereAdressdaten.setArt(Adressart.ADRESSE);
            andereAdressdaten.setAdresse("Sendlinger Straße");
            andereAdressdaten.setHausnummerVon("1");
            andereAdresse.addNutzung(Nutzung.BAUZAUN);

            final ProjektAdresseEntity gemeinsameAdresse = new ProjektAdresseEntity();
            gemeinsameAdresse.setAnzahlMahnungen(0);
            gemeinsameAdresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable gemeinsameAdressdaten = gemeinsameAdresse.getAdressdaten();
            gemeinsameAdressdaten.setArt(Adressart.ADRESSE);
            gemeinsameAdressdaten.setAdresse("Marienplatz");
            gemeinsameAdressdaten.setHausnummerVon("8");
            gemeinsameAdresse.addNutzung(Nutzung.CONTAINER);

            final ProjektEntity projektMitGemeinsamerAdresse = new ProjektEntity();
            projektMitGemeinsamerAdresse.setProjektnummer("2026-0002");
            projektMitGemeinsamerAdresse.setAbrechnungBeginn(BEGINN);
            projektMitGemeinsamerAdresse.setAbrechnungEnde(ENDE);
            projektMitGemeinsamerAdresse.addAdresse(andereAdresse);
            projektMitGemeinsamerAdresse.addAdresse(gemeinsameAdresse);
            projektRepository.save(projektMitGemeinsamerAdresse);

            final ProjektAdresseEntity andereHausnummer = new ProjektAdresseEntity();
            andereHausnummer.setAnzahlMahnungen(0);
            andereHausnummer.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable andereHausnummerAdressdaten = andereHausnummer.getAdressdaten();
            andereHausnummerAdressdaten.setArt(Adressart.ADRESSE);
            andereHausnummerAdressdaten.setAdresse("Marienplatz");
            andereHausnummerAdressdaten.setHausnummerVon("8");
            andereHausnummerAdressdaten.setHausnummerBis("10");
            andereHausnummer.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektMitAndererHausnummer = new ProjektEntity();
            projektMitAndererHausnummer.setProjektnummer("2026-0003");
            projektMitAndererHausnummer.setAbrechnungBeginn(BEGINN);
            projektMitAndererHausnummer.setAbrechnungEnde(ENDE);
            projektMitAndererHausnummer.addAdresse(andereHausnummer);
            projektRepository.save(projektMitAndererHausnummer);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0002"));
        }

        @Test
        void givenSameAdresseInDifferentCase_thenReturnTheProjekt() {
            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("MARIENPLATZ");
            adressdaten.setHausnummerVon("8");
            adresse.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt = new ProjektEntity();
            projekt.setProjektnummer("2026-0002");
            projekt.setAbrechnungBeginn(BEGINN);
            projekt.setAbrechnungEnde(ENDE);
            projekt.addAdresse(adresse);
            projektRepository.save(projekt);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0002"));
        }

        @Test
        void givenFlurstueck_thenReturnOnlyTheProjekteWithTheSameFlurstueckInTheSameGemarkung() {
            final ProjektAdresseEntity flurstueck = new ProjektAdresseEntity();
            flurstueck.setAnzahlMahnungen(0);
            flurstueck.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable flurstueckAdressdaten = flurstueck.getAdressdaten();
            flurstueckAdressdaten.setArt(Adressart.FLURSTUECK);
            flurstueckAdressdaten.setFlurstueck("1234/5");
            flurstueckAdressdaten.setGemarkung("Sendling");
            flurstueck.addNutzung(Nutzung.CONTAINER);

            final ProjektEntity projektMitFlurstueck = new ProjektEntity();
            projektMitFlurstueck.setProjektnummer("2026-0002");
            projektMitFlurstueck.setAbrechnungBeginn(BEGINN);
            projektMitFlurstueck.setAbrechnungEnde(ENDE);
            projektMitFlurstueck.addAdresse(flurstueck);
            final UUID projektMitFlurstueckId = projektRepository.save(projektMitFlurstueck).getId();

            final ProjektAdresseEntity gleichesFlurstueck = new ProjektAdresseEntity();
            gleichesFlurstueck.setAnzahlMahnungen(0);
            gleichesFlurstueck.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable gleichesFlurstueckAdressdaten = gleichesFlurstueck.getAdressdaten();
            gleichesFlurstueckAdressdaten.setArt(Adressart.FLURSTUECK);
            gleichesFlurstueckAdressdaten.setFlurstueck("1234/5");
            gleichesFlurstueckAdressdaten.setGemarkung("Sendling");
            gleichesFlurstueck.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektMitGleichemFlurstueck = new ProjektEntity();
            projektMitGleichemFlurstueck.setProjektnummer("2026-0003");
            projektMitGleichemFlurstueck.setAbrechnungBeginn(BEGINN);
            projektMitGleichemFlurstueck.setAbrechnungEnde(ENDE);
            projektMitGleichemFlurstueck.addAdresse(gleichesFlurstueck);
            projektRepository.save(projektMitGleichemFlurstueck);

            final ProjektAdresseEntity andereGemarkung = new ProjektAdresseEntity();
            andereGemarkung.setAnzahlMahnungen(0);
            andereGemarkung.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable andereGemarkungAdressdaten = andereGemarkung.getAdressdaten();
            andereGemarkungAdressdaten.setArt(Adressart.FLURSTUECK);
            andereGemarkungAdressdaten.setFlurstueck("1234/5");
            andereGemarkungAdressdaten.setGemarkung("Schwabing");
            andereGemarkung.addNutzung(Nutzung.CONTAINER);

            final ProjektEntity projektMitAndererGemarkung = new ProjektEntity();
            projektMitAndererGemarkung.setProjektnummer("2026-0004");
            projektMitAndererGemarkung.setAbrechnungBeginn(BEGINN);
            projektMitAndererGemarkung.setAbrechnungEnde(ENDE);
            projektMitAndererGemarkung.addAdresse(andereGemarkung);
            projektRepository.save(projektMitAndererGemarkung);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projektMitFlurstueckId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0003"));
        }

        @Test
        void givenProjektWithNachfolger_thenLeaveItOut() {
            final ProjektAdresseEntity adresseNachfolger = new ProjektAdresseEntity();
            adresseNachfolger.setAnzahlMahnungen(0);
            adresseNachfolger.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdatenNachfolger = adresseNachfolger.getAdressdaten();
            adressdatenNachfolger.setArt(Adressart.ADRESSE);
            adressdatenNachfolger.setAdresse("Sendlinger Straße");
            adressdatenNachfolger.setHausnummerVon("1");
            adresseNachfolger.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity nachfolger = new ProjektEntity();
            nachfolger.setProjektnummer("2026-0002");
            nachfolger.setAbrechnungBeginn(BEGINN);
            nachfolger.setAbrechnungEnde(ENDE);
            nachfolger.setVorgaengerProjektId(projektId);
            nachfolger.addAdresse(adresseNachfolger);
            projektRepository.save(nachfolger);

            final ProjektAdresseEntity adresseOhneVorgaenger = new ProjektAdresseEntity();
            adresseOhneVorgaenger.setAnzahlMahnungen(0);
            adresseOhneVorgaenger.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdatenOhneVorgaenger = adresseOhneVorgaenger.getAdressdaten();
            adressdatenOhneVorgaenger.setArt(Adressart.ADRESSE);
            adressdatenOhneVorgaenger.setAdresse("Marienplatz");
            adressdatenOhneVorgaenger.setHausnummerVon("8");
            adresseOhneVorgaenger.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektOhneVorgaenger = new ProjektEntity();
            projektOhneVorgaenger.setProjektnummer("2026-0003");
            projektOhneVorgaenger.setAbrechnungBeginn(BEGINN);
            projektOhneVorgaenger.setAbrechnungEnde(ENDE);
            projektOhneVorgaenger.addAdresse(adresseOhneVorgaenger);
            final UUID projektOhneVorgaengerId = projektRepository.save(projektOhneVorgaenger).getId();

            final ProjektAdresseEntity adresseOhneNachfolger = new ProjektAdresseEntity();
            adresseOhneNachfolger.setAnzahlMahnungen(0);
            adresseOhneNachfolger.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdatenOhneNachfolger = adresseOhneNachfolger.getAdressdaten();
            adressdatenOhneNachfolger.setArt(Adressart.ADRESSE);
            adressdatenOhneNachfolger.setAdresse("Marienplatz");
            adressdatenOhneNachfolger.setHausnummerVon("8");
            adresseOhneNachfolger.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projektOhneNachfolger = new ProjektEntity();
            projektOhneNachfolger.setProjektnummer("2026-0004");
            projektOhneNachfolger.setAbrechnungBeginn(BEGINN);
            projektOhneNachfolger.setAbrechnungEnde(ENDE);
            projektOhneNachfolger.addAdresse(adresseOhneNachfolger);
            projektRepository.save(projektOhneNachfolger);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projektOhneVorgaengerId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0004"));
        }

        @Test
        void givenSeveralVorschlaege_thenReturnTheHighestProjektnummerFirst() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0003", "2026-0002"));
        }

        @Test
        void givenVorschlagSharingSeveralAdressen_thenReturnItOnce() {
            final ProjektAdresseEntity marienplatz0002 = new ProjektAdresseEntity();
            marienplatz0002.setAnzahlMahnungen(0);
            marienplatz0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable marienplatzAdressdaten0002 = marienplatz0002.getAdressdaten();
            marienplatzAdressdaten0002.setArt(Adressart.ADRESSE);
            marienplatzAdressdaten0002.setAdresse("Marienplatz");
            marienplatzAdressdaten0002.setHausnummerVon("8");
            marienplatz0002.addNutzung(Nutzung.BAUZAUN);

            final ProjektAdresseEntity sendlingerStrasse0002 = new ProjektAdresseEntity();
            sendlingerStrasse0002.setAnzahlMahnungen(0);
            sendlingerStrasse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable sendlingerStrasseAdressdaten0002 = sendlingerStrasse0002.getAdressdaten();
            sendlingerStrasseAdressdaten0002.setArt(Adressart.ADRESSE);
            sendlingerStrasseAdressdaten0002.setAdresse("Sendlinger Straße");
            sendlingerStrasseAdressdaten0002.setHausnummerVon("1");
            sendlingerStrasse0002.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(marienplatz0002);
            projekt0002.addAdresse(sendlingerStrasse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            final ProjektAdresseEntity marienplatz0003 = new ProjektAdresseEntity();
            marienplatz0003.setAnzahlMahnungen(0);
            marienplatz0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable marienplatzAdressdaten0003 = marienplatz0003.getAdressdaten();
            marienplatzAdressdaten0003.setArt(Adressart.ADRESSE);
            marienplatzAdressdaten0003.setAdresse("Marienplatz");
            marienplatzAdressdaten0003.setHausnummerVon("8");
            marienplatz0003.addNutzung(Nutzung.BAUZAUN);

            final ProjektAdresseEntity sendlingerStrasse0003 = new ProjektAdresseEntity();
            sendlingerStrasse0003.setAnzahlMahnungen(0);
            sendlingerStrasse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable sendlingerStrasseAdressdaten0003 = sendlingerStrasse0003.getAdressdaten();
            sendlingerStrasseAdressdaten0003.setArt(Adressart.ADRESSE);
            sendlingerStrasseAdressdaten0003.setAdresse("Sendlinger Straße");
            sendlingerStrasseAdressdaten0003.setHausnummerVon("1");
            sendlingerStrasse0003.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(marienplatz0003);
            projekt0003.addAdresse(sendlingerStrasse0003);
            projektRepository.save(projekt0003);

            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$[*].projektnummer")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, projektnummern -> assertThat(projektnummern).containsExactly("2026-0003", "2026-0001"));
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            restTestClient.get()
                    .uri("/projekt/{projektId}/vorgaenger-vorschlaege", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    class SaveProjekt {
        @Test
        void givenProjekt_thenProjektIsSaved() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    BEGINN, ENDE, null, 3, true);
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
                final AdressdatenEmbeddable adressdaten = adresse.getAdressdaten();
                assertThat(adressdaten.getArt()).isEqualTo(Adressart.FLURSTUECK);
                assertThat(adressdaten.getFlurstueck()).isEqualTo("1234/5");
                assertThat(adressdaten.getGemarkung()).isEqualTo("Sendling");
                assertThat(adresse.getNutzungen()).containsExactly(Nutzung.CONTAINER);
                assertThat(adressdaten.getUnerlaubteNutzungVon()).isEqualTo(BEGINN);
                assertThat(adressdaten.getUnerlaubteNutzungBis()).isEqualTo(ENDE);
                assertThat(adresse.getAnzahlMahnungen()).isEqualTo(3);
                assertThat(adresse.isSondernutzungErlaubt()).isTrue();
            });
        }

        @Test
        void givenSeveralNutzungen_thenAllOfThemAreSavedWithTheBeschreibung() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null,
                    List.of(Nutzung.SONSTIGES, Nutzung.BAUZAUN, Nutzung.CONTAINER), "Gerüst über dem Gehweg",
                    null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0003", BEGINN, ENDE, List.of(adresseDTO));

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
            transactionTemplate.executeWithoutResult(status -> {
                final ProjektEntity projekt = projektRepository.findById(responseDTO.id()).orElseThrow();
                final ProjektAdresseEntity adresse = projekt.getAdressen().getFirst();
                assertThat(adresse.getNutzungen())
                        .containsExactlyInAnyOrder(Nutzung.BAUZAUN, Nutzung.CONTAINER, Nutzung.SONSTIGES);
                assertThat(adresse.getNutzungSonstiges()).isEqualTo("Gerüst über dem Gehweg");
            });
        }

        @Test
        void givenBeschreibungWithAndererNutzung_thenReturnBadRequest() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.BAUZAUN), "Gerüst über dem Gehweg",
                    null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0004", BEGINN, ENDE, List.of(adresseDTO));

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
        void givenInvertedAbrechnungszeitraum_thenReturnBadRequest() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    null, null, null, 0, false);
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
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    null, null, 12, 0, false);
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
                final AdressdatenEmbeddable persisted = projekt.getAdressen().getFirst().getAdressdaten();
                assertThat(persisted.getTageUnerlaubteNutzung()).isEqualTo(12);
                assertThat(persisted.getUnerlaubteNutzungVon()).isNull();
            });
        }

        @Test
        void givenZeitraum_thenTageAreDerivedFromIt() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, 0, false);
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
                            .getAdressen().getFirst().getAdressdaten().getTageUnerlaubteNutzung())
                    .isEqualTo(31));
        }

        @Test
        void givenZeitraumAndTage_thenReturnBadRequest() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", List.of(Nutzung.CONTAINER), null,
                    BEGINN, ENDE, 12, 0, false);
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

    @Nested
    class UpdateVorgaengerProjekt {
        @Test
        void givenStandaloneProjekt_thenPutItBehindTheVorgaenger() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projektId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.id()).isEqualTo(projekt0002Id);
                        assertThat(projektResponseDTO.vorgaengerProjektId()).isEqualTo(projektId);
                        assertThat(projektResponseDTO.vorgaengerProjekte())
                                .containsExactly(new LinkedProjektResponseDTO(projektId, "2026-0001"));
                        assertThat(projektResponseDTO.nachfolgerProjekte()).isEmpty();
                    });

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().getVorgaengerProjektId())
                    .isEqualTo(projektId));
        }

        @Test
        void givenLastLinkedProjektAsVorgaenger_thenLinkTheProjektBehindIt() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0003Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projekt0002Id))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.vorgaengerProjektId()).isEqualTo(projekt0002Id);
                        assertThat(projektResponseDTO.vorgaengerProjekte()).containsExactly(
                                new LinkedProjektResponseDTO(projektId, "2026-0001"),
                                new LinkedProjektResponseDTO(projekt0002Id, "2026-0002"));
                    });

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0003Id).orElseThrow().getVorgaengerProjektId())
                    .isEqualTo(projekt0002Id));
        }

        @Test
        void givenProjektItself_thenReturnBadRequest() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projektId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projektId).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenNoVorgaengerProjektId_thenReturnBadRequest() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(null))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projektId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenUnknownVorgaenger_thenReturnNotFound() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(UUID.randomUUID()))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projektId).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenVorgaengerWithNachfolger_thenReturnConflict() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0003Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projektId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0003Id).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenProjektWithNachfolger_thenReturnConflict() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            projektRepository.save(projekt0002);

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projekt0003Id))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projektId).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenProjektWithVorgaenger_thenReturnConflictAndKeepItsVorgaenger() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.addAdresse(adresse0003);
            final UUID projekt0003Id = projektRepository.save(projekt0003).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projekt0003Id))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().getVorgaengerProjektId())
                    .isEqualTo(projektId));
        }

        @Test
        void givenAbgelehnteVerknuepfung_thenWithdrawTheDecision() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVerknuepfungAbgelehnt(true);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(new VorgaengerProjektRequestDTO(projektId))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.verknuepfungAbgelehnt()).isFalse();
                    });

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().isVerknuepfungAbgelehnt())
                    .isFalse());
        }
    }

    @Nested
    class UpdateVerknuepfungAbgelehnt {
        @Test
        void givenStandaloneProjekt_thenStoreTheDecision() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/verknuepfung-abgelehnt", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(ProjektResponseDTO.class)
                    .value(projektResponseDTO -> {
                        assertNotNull(projektResponseDTO);
                        assertThat(projektResponseDTO.id()).isEqualTo(projektId);
                        assertThat(projektResponseDTO.verknuepfungAbgelehnt()).isTrue();
                    });

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projektId).orElseThrow().isVerknuepfungAbgelehnt())
                    .isTrue());
        }

        @Test
        void givenProjektWithVorgaenger_thenReturnConflictAndStoreNothing() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            restTestClient.put()
                    .uri("/projekt/{projektId}/verknuepfung-abgelehnt", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().isVerknuepfungAbgelehnt())
                    .isFalse());
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            restTestClient.put()
                    .uri("/projekt/{projektId}/verknuepfung-abgelehnt", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    class DeleteVorgaengerProjekt {
        @Test
        void givenLastLinkedProjekt_thenReleaseIt() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            restTestClient.delete()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .exchange()
                    .expectStatus().isNoContent();

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenProjektWithNachfolger_thenReturnConflictAndKeepItsVorgaenger() {
            final ProjektAdresseEntity adresse0002 = new ProjektAdresseEntity();
            adresse0002.setAnzahlMahnungen(0);
            adresse0002.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0002 = adresse0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adresse0002.addNutzung(Nutzung.BAUZAUN);
            adressdaten0002.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0002.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0002 = new ProjektEntity();
            projekt0002.setProjektnummer("2026-0002");
            projekt0002.setAbrechnungBeginn(BEGINN);
            projekt0002.setAbrechnungEnde(ENDE);
            projekt0002.setVorgaengerProjektId(projektId);
            projekt0002.addAdresse(adresse0002);
            final UUID projekt0002Id = projektRepository.save(projekt0002).getId();

            final ProjektAdresseEntity adresse0003 = new ProjektAdresseEntity();
            adresse0003.setAnzahlMahnungen(0);
            adresse0003.setSondernutzungErlaubt(false);

            final AdressdatenEmbeddable adressdaten0003 = adresse0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adresse0003.addNutzung(Nutzung.BAUZAUN);
            adressdaten0003.setUnerlaubteNutzungVon(BEGINN);
            adressdaten0003.setUnerlaubteNutzungBis(ENDE);

            final ProjektEntity projekt0003 = new ProjektEntity();
            projekt0003.setProjektnummer("2026-0003");
            projekt0003.setAbrechnungBeginn(BEGINN);
            projekt0003.setAbrechnungEnde(ENDE);
            projekt0003.setVorgaengerProjektId(projekt0002Id);
            projekt0003.addAdresse(adresse0003);
            projektRepository.save(projekt0003);

            restTestClient.delete()
                    .uri("/projekt/{projektId}/vorgaenger", projekt0002Id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projekt0002Id).orElseThrow().getVorgaengerProjektId())
                    .isEqualTo(projektId));
        }

        @Test
        void givenStandaloneProjekt_thenKeepItStandalone() {
            restTestClient.delete()
                    .uri("/projekt/{projektId}/vorgaenger", projektId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .exchange()
                    .expectStatus().isNoContent();

            transactionTemplate.executeWithoutResult(status -> assertThat(
                    projektRepository.findById(projektId).orElseThrow().getVorgaengerProjektId()).isNull());
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            restTestClient.delete()
                    .uri("/projekt/{projektId}/vorgaenger", UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

}
