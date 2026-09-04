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
    class GetAbrechnungenPage {

        @Test
        void givenAbrechnungenOfSeveralProjekte_thenReturnOnlyThoseOfTheRequestedProjekt() {
            final AbrechnungPositionEntity eigenePosition = new AbrechnungPositionEntity();
            eigenePosition.setBeginn(VON);
            eigenePosition.setEnde(BIS);
            eigenePosition.setLaenge(new BigDecimal("12.00"));
            eigenePosition.setBreite(new BigDecimal("3.00"));
            eigenePosition.setFlaeche(new BigDecimal("36.00"));
            eigenePosition.setHaelfte(true);
            eigenePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity eigenesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            eigenesNutzungsobjekt.addPosition(eigenePosition);

            final AdressdatenEmbeddable eigeneAdressdaten = eigenesNutzungsobjekt.getAdressdaten();
            eigeneAdressdaten.setArt(Adressart.ADRESSE);
            eigeneAdressdaten.setAdresse("Marienplatz");
            eigeneAdressdaten.setHausnummerVon("8");
            eigeneAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity eigeneAbrechnung = new AbrechnungEntity();
            eigeneAbrechnung.setProjektId(projektId);
            eigeneAbrechnung.setGeschaeftspartnerId("1000000001");
            eigeneAbrechnung.setZeitraumVon(VON);
            eigeneAbrechnung.setZeitraumBis(BIS);
            eigeneAbrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            eigeneAbrechnung.addNutzungsobjekt(eigenesNutzungsobjekt);
            abrechnungRepository.save(eigeneAbrechnung);

            final ProjektEntity anderesProjekt = new ProjektEntity();
            anderesProjekt.setProjektnummer("2026-0002");
            anderesProjekt.setAbrechnungBeginn(VON);
            anderesProjekt.setAbrechnungEnde(BIS);

            final ProjektAdresseEntity adresse = new ProjektAdresseEntity();
            adresse.setAnzahlMahnungen(0);
            adresse.setSondernutzungErlaubt(false);
            adresse.getAdressdaten().setArt(Adressart.ADRESSE);
            adresse.getAdressdaten().setAdresse("Sendlinger Straße");
            adresse.getAdressdaten().setHausnummerVon("1");
            anderesProjekt.addAdresse(adresse);

            final UUID anderesProjektId = projektRepository.save(anderesProjekt).getId();

            final AbrechnungEntity fremdeAbrechnung = new AbrechnungEntity();
            fremdeAbrechnung.setProjektId(anderesProjektId);
            fremdeAbrechnung.setGeschaeftspartnerId("1000000002");
            fremdeAbrechnung.setZeitraumVon(VON);
            fremdeAbrechnung.setZeitraumBis(BIS);
            fremdeAbrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            final AbrechnungPositionEntity fremdePosition = new AbrechnungPositionEntity();
            fremdePosition.setBeginn(VON);
            fremdePosition.setEnde(BIS);
            fremdePosition.setLaenge(new BigDecimal("12.00"));
            fremdePosition.setBreite(new BigDecimal("3.00"));
            fremdePosition.setFlaeche(new BigDecimal("36.00"));
            fremdePosition.setHaelfte(true);
            fremdePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity fremdesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            fremdesNutzungsobjekt.addPosition(fremdePosition);

            final AdressdatenEmbeddable fremdeAdressdaten = fremdesNutzungsobjekt.getAdressdaten();
            fremdeAdressdaten.setArt(Adressart.ADRESSE);
            fremdeAdressdaten.setAdresse("Sendlinger Straße");
            fremdeAdressdaten.setHausnummerVon("1");
            fremdeAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            fremdeAbrechnung.addNutzungsobjekt(fremdesNutzungsobjekt);
            abrechnungRepository.save(fremdeAbrechnung);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody()
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds).containsExactly("1000000001"));
        }

        @Test
        void givenNoSortParameters_thenPageByDescendingZeitraumVonWithoutRepeatingOrSkipping() {
            final AbrechnungPositionEntity position0001 = new AbrechnungPositionEntity();
            position0001.setBeginn(VON);
            position0001.setEnde(BIS);
            position0001.setLaenge(new BigDecimal("12.00"));
            position0001.setBreite(new BigDecimal("3.00"));
            position0001.setFlaeche(new BigDecimal("36.00"));
            position0001.setHaelfte(true);
            position0001.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt0001 = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt0001.addPosition(position0001);

            final AdressdatenEmbeddable adressdaten0001 = nutzungsobjekt0001.getAdressdaten();
            adressdaten0001.setArt(Adressart.ADRESSE);
            adressdaten0001.setAdresse("Marienplatz");
            adressdaten0001.setHausnummerVon("8");
            adressdaten0001.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung0001 = new AbrechnungEntity();
            abrechnung0001.setProjektId(projektId);
            abrechnung0001.setGeschaeftspartnerId("1000000001");
            abrechnung0001.setZeitraumVon(LocalDate.of(2026, 1, 1));
            abrechnung0001.setZeitraumBis(BIS);
            abrechnung0001.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0001.addNutzungsobjekt(nutzungsobjekt0001);
            abrechnungRepository.save(abrechnung0001);

            final AbrechnungPositionEntity position0002 = new AbrechnungPositionEntity();
            position0002.setBeginn(VON);
            position0002.setEnde(BIS);
            position0002.setLaenge(new BigDecimal("12.00"));
            position0002.setBreite(new BigDecimal("3.00"));
            position0002.setFlaeche(new BigDecimal("36.00"));
            position0002.setHaelfte(true);
            position0002.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt0002 = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt0002.addPosition(position0002);

            final AdressdatenEmbeddable adressdaten0002 = nutzungsobjekt0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adressdaten0002.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung0002 = new AbrechnungEntity();
            abrechnung0002.setProjektId(projektId);
            abrechnung0002.setGeschaeftspartnerId("1000000002");
            abrechnung0002.setZeitraumVon(LocalDate.of(2026, 3, 1));
            abrechnung0002.setZeitraumBis(BIS);
            abrechnung0002.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0002.addNutzungsobjekt(nutzungsobjekt0002);
            abrechnungRepository.save(abrechnung0002);

            final AbrechnungPositionEntity position0003 = new AbrechnungPositionEntity();
            position0003.setBeginn(VON);
            position0003.setEnde(BIS);
            position0003.setLaenge(new BigDecimal("12.00"));
            position0003.setBreite(new BigDecimal("3.00"));
            position0003.setFlaeche(new BigDecimal("36.00"));
            position0003.setHaelfte(true);
            position0003.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt0003 = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt0003.addPosition(position0003);

            final AdressdatenEmbeddable adressdaten0003 = nutzungsobjekt0003.getAdressdaten();
            adressdaten0003.setArt(Adressart.ADRESSE);
            adressdaten0003.setAdresse("Marienplatz");
            adressdaten0003.setHausnummerVon("8");
            adressdaten0003.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung0003 = new AbrechnungEntity();
            abrechnung0003.setProjektId(projektId);
            abrechnung0003.setGeschaeftspartnerId("1000000003");
            abrechnung0003.setZeitraumVon(LocalDate.of(2026, 2, 1));
            abrechnung0003.setZeitraumBis(BIS);
            abrechnung0003.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0003.addNutzungsobjekt(nutzungsobjekt0003);
            abrechnungRepository.save(abrechnung0003);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("pageNumber", "0")
                            .queryParam("pageSize", "2")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds).containsExactly("1000000002", "1000000003"));

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "2")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds).containsExactly("1000000001"));
        }

        @Test
        void givenZeitraumBisAscending_thenOrderByThatColumn() {
            final AbrechnungPositionEntity position0001 = new AbrechnungPositionEntity();
            position0001.setBeginn(VON);
            position0001.setEnde(BIS);
            position0001.setLaenge(new BigDecimal("12.00"));
            position0001.setBreite(new BigDecimal("3.00"));
            position0001.setFlaeche(new BigDecimal("36.00"));
            position0001.setHaelfte(true);
            position0001.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt0001 = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt0001.addPosition(position0001);

            final AdressdatenEmbeddable adressdaten0001 = nutzungsobjekt0001.getAdressdaten();
            adressdaten0001.setArt(Adressart.ADRESSE);
            adressdaten0001.setAdresse("Marienplatz");
            adressdaten0001.setHausnummerVon("8");
            adressdaten0001.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung0001 = new AbrechnungEntity();
            abrechnung0001.setProjektId(projektId);
            abrechnung0001.setGeschaeftspartnerId("1000000001");
            abrechnung0001.setZeitraumVon(VON);
            abrechnung0001.setZeitraumBis(LocalDate.of(2026, 6, 30));
            abrechnung0001.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0001.addNutzungsobjekt(nutzungsobjekt0001);
            abrechnungRepository.save(abrechnung0001);

            final AbrechnungPositionEntity position0002 = new AbrechnungPositionEntity();
            position0002.setBeginn(VON);
            position0002.setEnde(BIS);
            position0002.setLaenge(new BigDecimal("12.00"));
            position0002.setBreite(new BigDecimal("3.00"));
            position0002.setFlaeche(new BigDecimal("36.00"));
            position0002.setHaelfte(true);
            position0002.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt0002 = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt0002.addPosition(position0002);

            final AdressdatenEmbeddable adressdaten0002 = nutzungsobjekt0002.getAdressdaten();
            adressdaten0002.setArt(Adressart.ADRESSE);
            adressdaten0002.setAdresse("Marienplatz");
            adressdaten0002.setHausnummerVon("8");
            adressdaten0002.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung0002 = new AbrechnungEntity();
            abrechnung0002.setProjektId(projektId);
            abrechnung0002.setGeschaeftspartnerId("1000000002");
            abrechnung0002.setZeitraumVon(VON);
            abrechnung0002.setZeitraumBis(LocalDate.of(2026, 3, 31));
            abrechnung0002.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0002.addNutzungsobjekt(nutzungsobjekt0002);
            abrechnungRepository.save(abrechnung0002);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("sortBy", "ZEITRAUM_BIS")
                            .queryParam("sortDirection", "ASC")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds).containsExactly("1000000002", "1000000001"));
        }

        @Test
        void givenAbrechnungenPage_thenIncludeTheirNutzungsobjekte() {
            final AbrechnungPositionEntity position = new AbrechnungPositionEntity();
            position.setBeginn(VON);
            position.setEnde(BIS);
            position.setLaenge(new BigDecimal("12.00"));
            position.setBreite(new BigDecimal("3.00"));
            position.setFlaeche(new BigDecimal("36.00"));
            position.setHaelfte(true);
            position.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity nutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            nutzungsobjekt.addPosition(position);

            final AdressdatenEmbeddable adressdaten = nutzungsobjekt.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity abrechnung = new AbrechnungEntity();
            abrechnung.setProjektId(projektId);
            abrechnung.setGeschaeftspartnerId("1000000001");
            abrechnung.setZeitraumVon(VON);
            abrechnung.setZeitraumBis(BIS);
            abrechnung.setAbrechnungsArt(AbrechnungsArt.ZWISCHENABRECHNUNG);
            abrechnung.addNutzungsobjekt(nutzungsobjekt);
            abrechnungRepository.save(abrechnung);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.page.totalElements").isEqualTo(1)
                    .jsonPath("$.content[0].abrechnungsArt").isEqualTo("ZWISCHENABRECHNUNG")
                    .jsonPath("$.content[0].nutzungsobjekte.length()").isEqualTo(1)
                    .jsonPath("$.content[0].nutzungsobjekte[0].adresse").isEqualTo("Marienplatz");
        }

        @Test
        void givenProjektWithoutAbrechnungen_thenReturnEmptyPage() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.page.totalElements").isEqualTo(0);
        }

        @Test
        void givenUnknownProjekt_thenReturnNotFound() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(UUID.randomUUID()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenUnknownSortBy_thenRejectTheRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("sortBy", "GEHEIMES_FELD")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenPageSizeAboveMaximum_thenReturnBadRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("pageSize", "101")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        void givenNegativePageNumber_thenReturnBadRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("pageNumber", "-1")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
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
