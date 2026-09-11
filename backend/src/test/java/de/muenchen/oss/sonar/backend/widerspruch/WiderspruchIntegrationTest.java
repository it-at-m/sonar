package de.muenchen.oss.sonar.backend.widerspruch;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungNutzungsobjektEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungPositionEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.projekt.ProjektAdresseEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektEntity;
import de.muenchen.oss.sonar.backend.projekt.ProjektRepository;
import de.muenchen.oss.sonar.backend.widerspruch.dto.WiderspruchRequestDTO;
import de.muenchen.oss.sonar.backend.widerspruch.dto.WiderspruchResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.http.HttpStatus;
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
class WiderspruchIntegrationTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);
    private static final LocalDate EINGANG = LocalDate.of(2026, 4, 1);

    private static final String WIDERSPRUCH_PATH = "/abrechnung/{abrechnungId}/widerspruch";
    private static final String ABRECHNUNG_PATH = "/projekt/{projektId}/abrechnung";

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private WiderspruchRepository widerspruchRepository;

    @Autowired
    private AbrechnungRepository abrechnungRepository;

    @Autowired
    private ProjektRepository projektRepository;

    private UUID projektId;

    private UUID abrechnungId;

    @BeforeEach
    public void setUp() {
        widerspruchRepository.deleteAll();
        abrechnungRepository.deleteAll();
        projektRepository.deleteAll();

        final ProjektAdresseEntity projektAdresse = new ProjektAdresseEntity();
        projektAdresse.setAnzahlMahnungen(0);
        projektAdresse.setSondernutzungErlaubt(false);

        final AdressdatenEmbeddable projektAdressdaten = projektAdresse.getAdressdaten();
        projektAdressdaten.setArt(Adressart.ADRESSE);
        projektAdressdaten.setAdresse("Marienplatz");
        projektAdressdaten.setHausnummerVon("8");

        final ProjektEntity projekt = new ProjektEntity();
        projekt.setProjektnummer("2026-0001");
        projekt.setAbrechnungBeginn(VON);
        projekt.setAbrechnungEnde(BIS);
        projekt.addAdresse(projektAdresse);

        projektId = projektRepository.save(projekt).getId();

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
        abrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
        abrechnung.addNutzungsobjekt(nutzungsobjekt);

        abrechnungId = abrechnungRepository.save(abrechnung).getId();
    }

    @Nested
    class SaveWiderspruch {
        @Test
        void givenEveryField_thenWiderspruchIsSaved() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), "Abrechnung wird durchgeführt", true, true,
                    "Bemerkung");

            final WiderspruchResponseDTO responseDTO = restTestClient.post()
                    .uri(WIDERSPRUCH_PATH, abrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(WiderspruchResponseDTO.class)
                    .value(dto -> assertThat(dto.abrechnungId()).isEqualTo(abrechnungId))
                    .returnResult()
                    .getResponseBody();

            assertThat(responseDTO).isNotNull();

            final WiderspruchEntity persisted = widerspruchRepository.findById(responseDTO.id()).orElseThrow();
            assertThat(persisted.getAbrechnungId()).isEqualTo(abrechnungId);
            assertThat(persisted.getDatumEingang()).isEqualTo(EINGANG);
            assertThat(persisted.getDatumRuecknahme()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(persisted.getDatumVorlageRegierung()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(persisted.getDatumAblehnungRegierung()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(persisted.getEntscheidungDurchfuehrung()).isEqualTo("Abrechnung wird durchgeführt");
            assertThat(persisted.isSollAbgesetzt()).isTrue();
            assertThat(persisted.isNeueTeilabrechnungAnlegen()).isTrue();
            assertThat(persisted.getBemerkung()).isEqualTo("Bemerkung");
        }

        @Test
        void givenOnlyDatumEingang_thenWiderspruchIsSaved() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, null, false,
                    false, null);

            restTestClient.post()
                    .uri(WIDERSPRUCH_PATH, abrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(WiderspruchResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.datumEingang()).isEqualTo(EINGANG);
                        assertThat(dto.datumRuecknahme()).isNull();
                        assertThat(dto.entscheidungDurchfuehrung()).isNull();
                        assertThat(dto.sollAbgesetzt()).isFalse();
                        assertThat(dto.neueTeilabrechnungAnlegen()).isFalse();
                        assertThat(dto.bemerkung()).isNull();
                    });

            assertThat(widerspruchRepository.count()).isEqualTo(1);
        }

        @Test
        void givenNoDatumEingang_thenReturnBadRequest() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(null, null, null, null, null, false,
                    false, null);

            restTestClient.post()
                    .uri(WIDERSPRUCH_PATH, abrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(widerspruchRepository.count()).isZero();
        }

        @Test
        void givenUnknownAbrechnung_thenReturnNotFound() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, null, false,
                    false, null);

            restTestClient.post()
                    .uri(WIDERSPRUCH_PATH, UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();

            assertThat(widerspruchRepository.count()).isZero();
        }

        @Test
        void givenAbrechnungThatAlreadyHasOne_thenReturnConflict() {
            final WiderspruchEntity bestehenderWiderspruch = new WiderspruchEntity();
            bestehenderWiderspruch.setAbrechnungId(abrechnungId);
            bestehenderWiderspruch.setDatumEingang(EINGANG);
            widerspruchRepository.save(bestehenderWiderspruch);

            final WiderspruchRequestDTO zweiterWiderspruch = new WiderspruchRequestDTO(LocalDate.of(2026, 4, 20), null,
                    null, null, null, false, false, null);

            restTestClient.post()
                    .uri(WIDERSPRUCH_PATH, abrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(zweiterWiderspruch)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            assertThat(widerspruchRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    class WiderspruchVorhandenOnAbrechnung {
        @Test
        void givenAbrechnungWithoutWiderspruch_thenTheOverviewReportsNone() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[0].widerspruchVorhanden").isEqualTo(false);
        }

        @Test
        void givenAbrechnungWithWiderspruch_thenTheOverviewReportsIt() {
            final WiderspruchEntity widerspruch = new WiderspruchEntity();
            widerspruch.setAbrechnungId(abrechnungId);
            widerspruch.setDatumEingang(EINGANG);
            widerspruchRepository.save(widerspruch);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content[0].widerspruchVorhanden").isEqualTo(true);
        }
    }

}
