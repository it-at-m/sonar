package de.muenchen.oss.sonar.backend.geschaeftspartner.client;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MockGeschaeftspartnerClientTest {

    private final MockGeschaeftspartnerClient unitUnderTest = new MockGeschaeftspartnerClient();

    @Nested
    class FindById {
        @Test
        void givenAnyId_thenAnswerWithCannedData() {
            final Optional<GeschaeftspartnerResponseDTO> result = unitUnderTest.findById("GP-4711");

            assertThat(result).isPresent();
            assertThat(result.get().name1()).contains("GP-4711");
            assertThat(result.get().anrede()).isNotBlank();
            assertThat(result.get().strasse()).isNotBlank();
            assertThat(result.get().hausnummer()).isNotBlank();
            assertThat(result.get().postleitzahl()).isNotBlank();
            assertThat(result.get().ort()).isNotBlank();
            assertThat(result.get().land()).isNotBlank();
            assertThat(result.get().telefon()).isNotBlank();
            assertThat(result.get().email()).isNotBlank();
        }

        @Test
        void givenTheUnknownId_thenReturnEmpty() {
            assertThat(unitUnderTest.findById(MockGeschaeftspartnerClient.UNKNOWN_ID)).isEmpty();
        }

        @Test
        void givenBlankId_thenReturnEmpty() {
            assertThat(unitUnderTest.findById("   ")).isEmpty();
        }

        @Test
        void givenNullId_thenReturnEmpty() {
            assertThat(unitUnderTest.findById(null)).isEmpty();
        }
    }
}
