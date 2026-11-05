package de.muenchen.oss.sonar.backend.stadtbezirk;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import java.util.List;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
@Import(TestSecurityConfiguration.class)
class StadtbezirkIntegrationTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused")
    private static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer(
            DockerImageName.parse(TestConstants.TESTCONTAINERS_POSTGRES_IMAGE));

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private StadtbezirkRolleRepository stadtbezirkRolleRepository;

    @BeforeEach
    public void setUp() {
        stadtbezirkRolleRepository.deleteAll();
    }

    @Nested
    class GetStadtbezirkeOfCurrentUser {

        @Test
        void givenSeveralRoles_thenReturnTheStadtbezirkeOfAllOfThemOrderedByNumber() {
            final StadtbezirkRolleEntity sendlingSued = new StadtbezirkRolleEntity();
            sendlingSued.setStadtbezirk(Stadtbezirk.BEZIRK_06_SENDLING);
            sendlingSued.setRolle("SB_Sued");
            stadtbezirkRolleRepository.save(sendlingSued);

            final StadtbezirkRolleEntity altstadtLehelMitte = new StadtbezirkRolleEntity();
            altstadtLehelMitte.setStadtbezirk(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL);
            altstadtLehelMitte.setRolle("SB_Mitte");
            stadtbezirkRolleRepository.save(altstadtLehelMitte);

            final StadtbezirkRolleEntity moosachNord = new StadtbezirkRolleEntity();
            moosachNord.setStadtbezirk(Stadtbezirk.BEZIRK_10_MOOSACH);
            moosachNord.setRolle("SB_Nord");
            stadtbezirkRolleRepository.save(moosachNord);

            restTestClient.get()
                    .uri("/stadtbezirk")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte+SB_Sued")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(new ParameterizedTypeReference<List<Stadtbezirk>>() {
                    })
                    .value(stadtbezirke -> assertThat(stadtbezirke)
                            .containsExactly(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL, Stadtbezirk.BEZIRK_06_SENDLING));
        }

        @Test
        void givenNoSachbearbeiterRole_thenReturnNoStadtbezirk() {
            final StadtbezirkRolleEntity altstadtLehelMitte = new StadtbezirkRolleEntity();
            altstadtLehelMitte.setStadtbezirk(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL);
            altstadtLehelMitte.setRolle("SB_Mitte");
            stadtbezirkRolleRepository.save(altstadtLehelMitte);

            restTestClient.get()
                    .uri("/stadtbezirk")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(new ParameterizedTypeReference<List<Stadtbezirk>>() {
                    })
                    .value(stadtbezirke -> assertThat(stadtbezirke).isEmpty());
        }
    }
}
