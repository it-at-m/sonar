package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GeschaeftspartnerServiceTest {

    @Mock
    private GeschaeftspartnerClient geschaeftspartnerClient;

    @InjectMocks
    private GeschaeftspartnerService unitUnderTest;

    @Nested
    class GetGeschaeftspartner {
        @Test
        void givenKnownId_thenReturnTheMasterData() {
            final GeschaeftspartnerResponseDTO geschaeftspartner = new GeschaeftspartnerResponseDTO(
                    "Firma", "Musterfirma", null, null, null, null, null, null,
                    "Musterstraße", "1", null, "80331", "München", "DE", null, null, null, null, null);
            when(geschaeftspartnerClient.findById("GP-4711")).thenReturn(Optional.of(geschaeftspartner));

            assertThat(unitUnderTest.getGeschaeftspartner("GP-4711")).isEqualTo(geschaeftspartner);
        }

        @Test
        void givenUnknownId_thenThrowNotFoundException() {
            when(geschaeftspartnerClient.findById("GP-0000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unitUnderTest.getGeschaeftspartner("GP-0000"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void givenUnreachableSystem_thenPropagateTheGatewayError() {
            when(geschaeftspartnerClient.findById("GP-4711"))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "kaputt", new IllegalStateException()));

            assertThatThrownBy(() -> unitUnderTest.getGeschaeftspartner("GP-4711"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        }
    }
}
