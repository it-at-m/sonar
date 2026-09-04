package de.muenchen.oss.sonar.backend.geschaeftspartner.client;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Wired in for the profiles "local" and "test", where no third party system exists to call. It
 * answers for every id so a form can be filled in without knowing any real master data, except for
 * {@link #UNKNOWN_ID}, which keeps the "not found" path reachable in the running application.
 */
@Slf4j
public class MockGeschaeftspartnerClient implements GeschaeftspartnerClient {

    public static final String UNKNOWN_ID = "000";

    @Override
    public Optional<GeschaeftspartnerResponseDTO> findById(final String geschaeftspartnerId) {
        log.debug("Answering the lookup of Geschaeftspartner {} with canned data", geschaeftspartnerId);
        if (geschaeftspartnerId == null || geschaeftspartnerId.isBlank() || UNKNOWN_ID.equals(geschaeftspartnerId)) {
            return Optional.empty();
        }
        return Optional.of(new GeschaeftspartnerResponseDTO(
                "Firma",
                "Musterfirma " + geschaeftspartnerId,
                "Zweigstelle Süd",
                null,
                null,
                null,
                null,
                null,
                "Musterstraße",
                "1",
                "Rückgebäude",
                "80331",
                "München",
                "DE",
                "+49 89 233-00",
                null,
                "+49 89 233-99",
                "notexist@muenchen.de",
                "Zustellung nur werktags"));
    }

}
