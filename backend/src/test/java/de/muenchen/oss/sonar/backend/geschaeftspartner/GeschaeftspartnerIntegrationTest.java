package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.muenchen.oss.sonar.backend.TestSecurityConfiguration;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.MockGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.projekt.ProjektRepository;
import de.muenchen.oss.sonar.backend.theentity.TheEntityRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration,"
                        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration" }
)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
@Import(TestSecurityConfiguration.class)
class GeschaeftspartnerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjektRepository projektRepository;

    @MockitoBean
    private TheEntityRepository theEntityRepository;

    @Nested
    class GetGeschaeftspartner {
        @Test
        void givenKnownId_thenReturnTheMasterData() throws Exception {
            mockMvc.perform(get("/geschaeftspartner/{geschaeftspartnerId}", "GP-4711")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.anrede").value("Firma"))
                    .andExpect(jsonPath("$.name1").value("Musterfirma GP-4711"))
                    .andExpect(jsonPath("$.name2").value("Zweigstelle Süd"))
                    .andExpect(jsonPath("$.strasse").value("Musterstraße"))
                    .andExpect(jsonPath("$.hausnummer").value("1"))
                    .andExpect(jsonPath("$.adresszusatz").value("Rückgebäude"))
                    .andExpect(jsonPath("$.postleitzahl").value("80331"))
                    .andExpect(jsonPath("$.ort").value("München"))
                    .andExpect(jsonPath("$.land").value("DE"))
                    .andExpect(jsonPath("$.telefon").value("+49 89 233-00"))
                    .andExpect(jsonPath("$.fax").value("+49 89 233-99"))
                    .andExpect(jsonPath("$.email").value("notexist@muenchen.de"))
                    .andExpect(jsonPath("$.adressnotiz").value("Zustellung nur werktags"));
        }

        @Test
        void givenUnknownId_thenReturnNotFound() throws Exception {
            mockMvc.perform(get("/geschaeftspartner/{geschaeftspartnerId}", MockGeschaeftspartnerClient.UNKNOWN_ID)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void givenIdLongerThanTheOwningSystemAllows_thenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/geschaeftspartner/{geschaeftspartnerId}", "12345678901")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer reader"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void givenNoToken_thenReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/geschaeftspartner/{geschaeftspartnerId}", "GP-4711"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
