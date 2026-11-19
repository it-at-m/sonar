package de.muenchen.oss.sonar.backend.stadtbezirk;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.TestConstants;
import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import java.util.List;
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

    @Nested
    class GetStadtbezirkeOfCurrentUser {

        @Test
        void givenSeveralRoles_thenReturnTheStadtbezirkeOfAllOfThemOrderedByNumber() {
            restTestClient.get()
                    .uri("/stadtbezirk")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer SB_Mitte+SB_Sued")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(new ParameterizedTypeReference<List<Stadtbezirk>>() {
                    })
                    .value(stadtbezirke -> assertThat(stadtbezirke)
                            .containsExactly(
                                    Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL,
                                    Stadtbezirk.BEZIRK_02_LUDWIGSVORSTADT_ISARVORSTADT,
                                    Stadtbezirk.BEZIRK_03_MAXVORSTADT,
                                    Stadtbezirk.BEZIRK_05_AU_HAIDHAUSEN,
                                    Stadtbezirk.BEZIRK_06_SENDLING,
                                    Stadtbezirk.BEZIRK_07_SENDLING_WESTPARK,
                                    Stadtbezirk.BEZIRK_08_SCHWANTHALERHOEHE,
                                    Stadtbezirk.BEZIRK_18_UNTERGIESING_HARLACHING,
                                    Stadtbezirk.BEZIRK_19_THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN,
                                    Stadtbezirk.BEZIRK_20_HADERN));
        }

        @Test
        void givenNoSachbearbeiterRole_thenReturnNoStadtbezirk() {
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
