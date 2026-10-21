package de.muenchen.oss.sonar.backend.berechnung;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * One case from {@code src/test/resources/abrechnung-testdaten}: the recorded input of the
 * Berechnung plus the values the result has to contain.
 */
public record AbrechnungTestfall(
        String fileName,
        String name,
        JsonNode input,
        JsonNode letzteAbrechnung,
        List<Expectation> expectations) {

    public record Expectation(String jsonPath, JsonNode value) {
    }

    public static final String BASE_PATH = "abrechnung-testdaten";

    private static final String FAELLE_RESOURCES = "classpath:" + BASE_PATH + "/faelle/*.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String toString() {
        return name + " (" + fileName + ")";
    }

    public static List<AbrechnungTestfall> loadAll() {
        try {
            final Resource[] resources = new PathMatchingResourcePatternResolver().getResources(FAELLE_RESOURCES);
            final List<AbrechnungTestfall> testfaelle = new ArrayList<>();
            for (final Resource resource : resources) {
                testfaelle.add(read(resource));
            }
            testfaelle.sort(Comparator.comparing(AbrechnungTestfall::fileName));
            return testfaelle;
        } catch (final IOException e) {
            throw new UncheckedIOException("Testdaten unter " + FAELLE_RESOURCES + " nicht lesbar", e);
        }
    }

    private static AbrechnungTestfall read(final Resource resource) throws IOException {
        final JsonNode root = MAPPER.readTree(resource.getInputStream());
        return new AbrechnungTestfall(
                resource.getFilename(),
                root.path("name").asString(),
                root.get("input"),
                root.get("letzteAbrechnung"),
                toExpectations(root.get("expected")));
    }

    private static List<Expectation> toExpectations(final JsonNode expected) {
        final List<Expectation> expectations = new ArrayList<>();
        if (expected == null) {
            return expectations;
        }
        for (final Map.Entry<String, JsonNode> property : expected.properties()) {
            expectations.add(new Expectation(property.getKey().trim(), property.getValue()));
        }
        return expectations;
    }

    public void check(final String responseJson) {
        final SoftAssertions softly = new SoftAssertions();
        final DocumentContext response = JsonPath.parse(responseJson);
        for (final Expectation expectation : expectations) {
            final JsonNode actual;
            try {
                actual = MAPPER.valueToTree(response.read(expectation.jsonPath()));
            } catch (final PathNotFoundException e) {
                softly.fail("%s -> %s: Pfad fehlt in der Antwort", fileName, expectation.jsonPath());
                continue;
            }
            final JsonNode expected = expectation.value();
            final String where = fileName + " -> " + expectation.jsonPath();

            if (expected.isNumber() && actual.isNumber()) {
                softly.assertThat(actual.decimalValue())
                        .as(where)
                        .isEqualByComparingTo(expected.decimalValue());
            } else if (isText(expected)) {
                softly.assertThat(normalizeLines(actual.asString()))
                        .as(where)
                        .isEqualTo(normalizeLines(toText(expected)));
            } else {
                softly.assertThat(actual).as(where).isEqualTo(expected);
            }
        }
        softly.assertAll();
    }

    private static boolean isText(final JsonNode expected) {
        return expected.isString() || (expected.isArray() && !expected.isEmpty() && expected.get(0).isString());
    }

    private static String toText(final JsonNode expected) {
        if (expected.isString()) {
            return expected.asString();
        }
        final List<String> lines = new ArrayList<>();
        expected.forEach(line -> lines.add(line.asString()));
        return String.join("\n", lines);
    }

    private static String normalizeLines(final String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

}
