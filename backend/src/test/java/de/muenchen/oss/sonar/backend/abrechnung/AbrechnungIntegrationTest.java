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
import java.util.ArrayList;
import java.util.Comparator;
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
class AbrechnungIntegrationTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private static final String ABRECHNUNG_PATH = "/projekt/{projektId}/abrechnung";
    private static final String ABRECHNUNG_BY_ID_PATH = "/projekt/{projektId}/abrechnung/{abrechnungId}";
    private static final String ABRECHNUNG_VERSION_PATH = "/projekt/{projektId}/abrechnung/{abrechnungId}/version";

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
        final List<AbrechnungEntity> gespeicherteAbrechnungen = new ArrayList<>();
        abrechnungRepository.findAll().forEach(gespeicherteAbrechnungen::add);
        gespeicherteAbrechnungen.stream()
                .sorted(Comparator.comparing(AbrechnungEntity::getVersionsnummer).reversed())
                .forEach(abrechnungRepository::delete);
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
            eigeneAbrechnung.setVersionsnummer(1);
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
            fremdeAbrechnung.setVersionsnummer(1);
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
            abrechnung0001.setVersionsnummer(1);
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
            abrechnung0002.setVersionsnummer(1);
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
            abrechnung0003.setVersionsnummer(1);
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
            abrechnung0001.setVersionsnummer(1);
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
            abrechnung0002.setVersionsnummer(1);
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
        void givenSeveralSortColumns_thenOrderByTheSecondColumnWithinTheFirst() {
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
            abrechnung0001.setVersionsnummer(1);
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
            abrechnung0002.setVersionsnummer(1);
            abrechnung0002.setGeschaeftspartnerId("1000000002");
            abrechnung0002.setZeitraumVon(LocalDate.of(2026, 3, 1));
            abrechnung0002.setZeitraumBis(BIS);
            abrechnung0002.setAbrechnungsArt(AbrechnungsArt.ZWISCHENABRECHNUNG);
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
            abrechnung0003.setVersionsnummer(1);
            abrechnung0003.setGeschaeftspartnerId("1000000003");
            abrechnung0003.setZeitraumVon(LocalDate.of(2026, 2, 1));
            abrechnung0003.setZeitraumBis(BIS);
            abrechnung0003.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung0003.addNutzungsobjekt(nutzungsobjekt0003);
            abrechnungRepository.save(abrechnung0003);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("sortBy", "ABRECHNUNGS_ART,ZEITRAUM_VON")
                            .queryParam("sortDirection", "ASC,DESC")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds)
                            .containsExactly("1000000003", "1000000001", "1000000002"));
        }

        @Test
        void givenMoreSortColumnsThanExist_thenAcceptTheRequest() {
            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .queryParam("sortBy", "ZEITRAUM_VON,ZEITRAUM_BIS,ABRECHNUNGS_ART,GESCHAEFTSPARTNER_ID,ZEITRAUM_VON")
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk();
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
            abrechnung.setVersionsnummer(1);
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

    @Nested
    class GetAbrechnung {

        @Test
        void givenAbrechnungOfProjekt_thenReturnItWithItsNutzungsobjekte() {
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
            abrechnung.setVersionsnummer(1);
            abrechnung.setGeschaeftspartnerId("1000000001");
            abrechnung.setZeitraumVon(VON);
            abrechnung.setZeitraumBis(BIS);
            abrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            abrechnung.addNutzungsobjekt(nutzungsobjekt);
            final UUID abrechnungId = abrechnungRepository.save(abrechnung).getId();

            restTestClient.get()
                    .uri(ABRECHNUNG_BY_ID_PATH, projektId, abrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(AbrechnungResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.id()).isEqualTo(abrechnungId);
                        assertThat(dto.versionsnummer()).isEqualTo(1);
                        assertThat(dto.neuereVersionVorhanden()).isFalse();
                        assertThat(dto.geschaeftspartnerId()).isEqualTo("1000000001");
                        assertThat(dto.nutzungsobjekte()).hasSize(1);
                        assertThat(dto.nutzungsobjekte().getFirst().adresse()).isEqualTo("Marienplatz");
                        assertThat(dto.nutzungsobjekte().getFirst().positionen().getFirst().flaeche())
                                .isEqualByComparingTo("36.00");
                    });
        }

        @Test
        void givenAbrechnungOfAnotherProjekt_thenReturnNotFound() {
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

            final AbrechnungEntity fremdeAbrechnung = new AbrechnungEntity();
            fremdeAbrechnung.setProjektId(projektId);
            fremdeAbrechnung.setVersionsnummer(1);
            fremdeAbrechnung.setGeschaeftspartnerId("1000000001");
            fremdeAbrechnung.setZeitraumVon(VON);
            fremdeAbrechnung.setZeitraumBis(BIS);
            fremdeAbrechnung.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            fremdeAbrechnung.addNutzungsobjekt(nutzungsobjekt);
            final UUID fremdeAbrechnungId = abrechnungRepository.save(fremdeAbrechnung).getId();

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

            restTestClient.get()
                    .uri(ABRECHNUNG_BY_ID_PATH, anderesProjektId, fremdeAbrechnungId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        void givenUnknownAbrechnung_thenReturnNotFound() {
            restTestClient.get()
                    .uri(ABRECHNUNG_BY_ID_PATH, projektId, UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    class SaveAbrechnungVersion {

        @Test
        void givenChangedData_thenSaveThemAsTheSecondVersion() {
            final AbrechnungPositionEntity erstePosition = new AbrechnungPositionEntity();
            erstePosition.setBeginn(VON);
            erstePosition.setEnde(BIS);
            erstePosition.setLaenge(new BigDecimal("12.00"));
            erstePosition.setBreite(new BigDecimal("3.00"));
            erstePosition.setFlaeche(new BigDecimal("36.00"));
            erstePosition.setHaelfte(true);
            erstePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity erstesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            erstesNutzungsobjekt.addPosition(erstePosition);

            final AdressdatenEmbeddable ersteAdressdaten = erstesNutzungsobjekt.getAdressdaten();
            ersteAdressdaten.setArt(Adressart.ADRESSE);
            ersteAdressdaten.setAdresse("Marienplatz");
            ersteAdressdaten.setHausnummerVon("8");
            ersteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ersteVersion = new AbrechnungEntity();
            ersteVersion.setProjektId(projektId);
            ersteVersion.setVersionsnummer(1);
            ersteVersion.setGeschaeftspartnerId("1000000001");
            ersteVersion.setZeitraumVon(VON);
            ersteVersion.setZeitraumBis(BIS);
            ersteVersion.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ersteVersion.addNutzungsobjekt(erstesNutzungsobjekt);
            final UUID ersteVersionId = abrechnungRepository.save(ersteVersion).getId();

            final AbrechnungNutzungsobjektRequestDTO geaendertesNutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Sendlinger Straße", "1", "3", null, null, Nutzung.NUTZUNG_B,
                    null, null, 5, "Zweite Fassung",
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("15.00"), new BigDecimal("3.00"),
                            new BigDecimal("45.00"), false, new BigDecimal("45.00"))));
            final AbrechnungRequestDTO neueVersionDTO = new AbrechnungRequestDTO("1000000002", false, null, null, VON, BIS,
                    AbrechnungsArt.ZWISCHENABRECHNUNG, List.of(geaendertesNutzungsobjekt));

            final AbrechnungResponseDTO neueVersion = restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(neueVersionDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(AbrechnungResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.id()).isNotEqualTo(ersteVersionId);
                        assertThat(dto.versionsnummer()).isEqualTo(2);
                        assertThat(dto.geschaeftspartnerId()).isEqualTo("1000000002");
                        assertThat(dto.abrechnungsArt()).isEqualTo(AbrechnungsArt.ZWISCHENABRECHNUNG);
                        assertThat(dto.nutzungsobjekte().getFirst().adresse()).isEqualTo("Sendlinger Straße");
                        assertThat(dto.nutzungsobjekte().getFirst().tageUnerlaubteNutzung()).isEqualTo(5);
                    })
                    .returnResult()
                    .getResponseBody();

            assertThat(neueVersion).isNotNull();
            transactionTemplate.executeWithoutResult(status -> assertThat(
                    abrechnungRepository.findById(neueVersion.id()).orElseThrow().getVorgaengerAbrechnungId())
                    .isEqualTo(ersteVersionId));
        }

        @Test
        void givenNewVersion_thenTheVorgaengerStaysAsItWas() {
            final AbrechnungPositionEntity erstePosition = new AbrechnungPositionEntity();
            erstePosition.setBeginn(VON);
            erstePosition.setEnde(BIS);
            erstePosition.setLaenge(new BigDecimal("12.00"));
            erstePosition.setBreite(new BigDecimal("3.00"));
            erstePosition.setFlaeche(new BigDecimal("36.00"));
            erstePosition.setHaelfte(true);
            erstePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity erstesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            erstesNutzungsobjekt.addPosition(erstePosition);
            erstesNutzungsobjekt.setBemerkung("Erste Fassung");

            final AdressdatenEmbeddable ersteAdressdaten = erstesNutzungsobjekt.getAdressdaten();
            ersteAdressdaten.setArt(Adressart.ADRESSE);
            ersteAdressdaten.setAdresse("Marienplatz");
            ersteAdressdaten.setHausnummerVon("8");
            ersteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ersteVersion = new AbrechnungEntity();
            ersteVersion.setProjektId(projektId);
            ersteVersion.setVersionsnummer(1);
            ersteVersion.setGeschaeftspartnerId("1000000001");
            ersteVersion.setZeitraumVon(VON);
            ersteVersion.setZeitraumBis(BIS);
            ersteVersion.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ersteVersion.addNutzungsobjekt(erstesNutzungsobjekt);
            final UUID ersteVersionId = abrechnungRepository.save(ersteVersion).getId();

            final AbrechnungNutzungsobjektRequestDTO geaendertesNutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, Nutzung.NUTZUNG_B,
                    null, null, null, "Zweite Fassung",
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("15.00"), new BigDecimal("3.00"),
                            new BigDecimal("45.00"), false, new BigDecimal("45.00"))));
            final AbrechnungRequestDTO neueVersionDTO = new AbrechnungRequestDTO("1000000002", false, null, null, VON, BIS,
                    AbrechnungsArt.ZWISCHENABRECHNUNG, List.of(geaendertesNutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(neueVersionDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated();

            restTestClient.get()
                    .uri(ABRECHNUNG_BY_ID_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AbrechnungResponseDTO.class)
                    .value(dto -> {
                        assertThat(dto.versionsnummer()).isEqualTo(1);
                        assertThat(dto.neuereVersionVorhanden()).isTrue();
                        assertThat(dto.geschaeftspartnerId()).isEqualTo("1000000001");
                        assertThat(dto.abrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
                        assertThat(dto.nutzungsobjekte().getFirst().adresse()).isEqualTo("Marienplatz");
                        assertThat(dto.nutzungsobjekte().getFirst().bemerkung()).isEqualTo("Erste Fassung");
                    });
        }

        @Test
        void givenNewVersion_thenOnlyItIsListedForTheProjekt() {
            final AbrechnungPositionEntity erstePosition = new AbrechnungPositionEntity();
            erstePosition.setBeginn(VON);
            erstePosition.setEnde(BIS);
            erstePosition.setLaenge(new BigDecimal("12.00"));
            erstePosition.setBreite(new BigDecimal("3.00"));
            erstePosition.setFlaeche(new BigDecimal("36.00"));
            erstePosition.setHaelfte(true);
            erstePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity erstesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            erstesNutzungsobjekt.addPosition(erstePosition);

            final AdressdatenEmbeddable ersteAdressdaten = erstesNutzungsobjekt.getAdressdaten();
            ersteAdressdaten.setArt(Adressart.ADRESSE);
            ersteAdressdaten.setAdresse("Marienplatz");
            ersteAdressdaten.setHausnummerVon("8");
            ersteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ersteVersion = new AbrechnungEntity();
            ersteVersion.setProjektId(projektId);
            ersteVersion.setVersionsnummer(1);
            ersteVersion.setGeschaeftspartnerId("1000000001");
            ersteVersion.setZeitraumVon(VON);
            ersteVersion.setZeitraumBis(BIS);
            ersteVersion.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ersteVersion.addNutzungsobjekt(erstesNutzungsobjekt);
            final UUID ersteVersionId = abrechnungRepository.save(ersteVersion).getId();

            final AbrechnungNutzungsobjektRequestDTO geaendertesNutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, Nutzung.NUTZUNG_B,
                    null, null, null, "Zweite Fassung",
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("15.00"), new BigDecimal("3.00"),
                            new BigDecimal("45.00"), false, new BigDecimal("45.00"))));
            final AbrechnungRequestDTO neueVersionDTO = new AbrechnungRequestDTO("1000000002", false, null, null, VON, BIS,
                    AbrechnungsArt.ZWISCHENABRECHNUNG, List.of(geaendertesNutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(neueVersionDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated();

            assertThat(abrechnungRepository.count()).isEqualTo(2);

            restTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ABRECHNUNG_PATH)
                            .build(projektId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.page.totalElements").isEqualTo(1)
                    .jsonPath("$.content[0].versionsnummer").isEqualTo(2)
                    .jsonPath("$.content[0].vorgaengerAbrechnungId").isEqualTo(ersteVersionId.toString())
                    .jsonPath("$.content..geschaeftspartnerId")
                    .value(new ParameterizedTypeReference<List<String>>() {
                    }, geschaeftspartnerIds -> assertThat(geschaeftspartnerIds).containsExactly("1000000002"));
        }

        @Test
        void givenAbrechnungThatAlreadyHasANewerVersion_thenReturnConflict() {
            final AbrechnungPositionEntity erstePosition = new AbrechnungPositionEntity();
            erstePosition.setBeginn(VON);
            erstePosition.setEnde(BIS);
            erstePosition.setLaenge(new BigDecimal("12.00"));
            erstePosition.setBreite(new BigDecimal("3.00"));
            erstePosition.setFlaeche(new BigDecimal("36.00"));
            erstePosition.setHaelfte(true);
            erstePosition.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungNutzungsobjektEntity erstesNutzungsobjekt = new AbrechnungNutzungsobjektEntity();
            erstesNutzungsobjekt.addPosition(erstePosition);

            final AdressdatenEmbeddable ersteAdressdaten = erstesNutzungsobjekt.getAdressdaten();
            ersteAdressdaten.setArt(Adressart.ADRESSE);
            ersteAdressdaten.setAdresse("Marienplatz");
            ersteAdressdaten.setHausnummerVon("8");
            ersteAdressdaten.setNutzung(Nutzung.NUTZUNG_A);

            final AbrechnungEntity ersteVersion = new AbrechnungEntity();
            ersteVersion.setProjektId(projektId);
            ersteVersion.setVersionsnummer(1);
            ersteVersion.setGeschaeftspartnerId("1000000001");
            ersteVersion.setZeitraumVon(VON);
            ersteVersion.setZeitraumBis(BIS);
            ersteVersion.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            ersteVersion.addNutzungsobjekt(erstesNutzungsobjekt);
            final UUID ersteVersionId = abrechnungRepository.save(ersteVersion).getId();

            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, Nutzung.NUTZUNG_B,
                    null, null, null, "Zweite Fassung",
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("15.00"), new BigDecimal("3.00"),
                            new BigDecimal("45.00"), false, new BigDecimal("45.00"))));
            final AbrechnungRequestDTO neueVersionDTO = new AbrechnungRequestDTO("1000000002", false, null, null, VON, BIS,
                    AbrechnungsArt.ZWISCHENABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(neueVersionDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isCreated();

            restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, ersteVersionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(neueVersionDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT);

            assertThat(abrechnungRepository.count()).isEqualTo(2);
        }

        @Test
        void givenUnknownAbrechnung_thenReturnNotFound() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null,
                    null, null, null, null,
                    List.of(new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"),
                            new BigDecimal("36.00"), true, new BigDecimal("30.00"))));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            restTestClient.post()
                    .uri(ABRECHNUNG_VERSION_PATH, projektId, UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer writer")
                    .body(requestDTO)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound();

            assertThat(abrechnungRepository.count()).isZero();
        }
    }

}
