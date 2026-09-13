package de.muenchen.oss.sonar.backend;

import static de.muenchen.oss.sonar.backend.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.projekt.ProjektRepository;
import de.muenchen.oss.sonar.backend.theentity.TheEntityRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Keeps the committed OpenAPI specification in sync with the code.
 * <p>
 * The specification is the contract the frontend client is generated from, so a controller or DTO
 * change that is not reflected in it would silently produce a wrong client. This test fetches the
 * live document and fails when it differs from the committed file.
 * </p>
 * <p>
 * Regenerate the file with:
 * {@code mvn clean test -Dtest=OpenApiSpecTest -Dopenapi.generate=true}
 * </p>
 * <p>
 * The clean is required. Javadoc reaches the document through an annotation processor, so class
 * files left by another build, an IDE for example, carry none of its output. Every summary and
 * description is then stripped without anything failing.
 * </p>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "springdoc.api-docs.enabled=true",
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
class OpenApiSpecTest {

    private static final Path SPEC_FILE = Path.of("api-spec", "sonar-backend.yaml");

    private static final String API_DOCS_PATH = "/v3/api-docs.yaml";

    private static final String GENERATE_PROPERTY = "openapi.generate";

    private static final String SERVER_URL = "http://localhost:8086";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbrechnungRepository abrechnungRepository;

    @MockitoBean
    private ProjektRepository projektRepository;

    @MockitoBean
    private TheEntityRepository theEntityRepository;

    @Test
    @DisabledIfSystemProperty(
            named = GENERATE_PROPERTY, matches = "true",
            disabledReason = "The specification is being regenerated in this run"
    )
    void givenApplicationContext_thenCommittedSpecificationIsUpToDate() throws Exception {
        final String expected = Files.readString(SPEC_FILE, StandardCharsets.UTF_8);

        final String generated = generateSpecification();

        assertThat(generated)
                .as("%s does not match the current API. Regenerate it with: mvn clean test -Dtest=%s -D%s=true",
                        SPEC_FILE, OpenApiSpecTest.class.getSimpleName(), GENERATE_PROPERTY)
                .isEqualTo(expected);
    }

    /**
     * A generator rather than a test. It writes the document into the committed file instead of
     * comparing the two, which is how the specification is updated.
     * {@link #givenApplicationContext_thenCommittedSpecificationIsUpToDate()} is disabled in the
     * same run, so regenerating never fails on the file it is about to replace. The assertion only
     * proves that something was written, because the new document is itself the expectation.
     */
    @Test
    @EnabledIfSystemProperty(named = GENERATE_PROPERTY, matches = "true")
    void givenGenerateProperty_thenWriteSpecification() throws Exception {
        final String generated = generateSpecification();
        Files.writeString(SPEC_FILE, generated, StandardCharsets.UTF_8);

        assertThat(SPEC_FILE).isNotEmptyFile();
    }

    private String generateSpecification() throws Exception {
        final String document = mockMvc.perform(get(API_DOCS_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return document.replaceFirst("(?m)^- url: http://localhost(:\\d+)?$", "- url: " + SERVER_URL);
    }
}
