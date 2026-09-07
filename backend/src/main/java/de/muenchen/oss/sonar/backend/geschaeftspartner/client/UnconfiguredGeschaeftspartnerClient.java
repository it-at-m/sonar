package de.muenchen.oss.sonar.backend.geschaeftspartner.client;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Wired in when no endpoint is configured, so the application starts and only this endpoint fails.
 */
@Slf4j
public class UnconfiguredGeschaeftspartnerClient implements GeschaeftspartnerClient {

    @Override
    public Optional<GeschaeftspartnerResponseDTO> findById(final String geschaeftspartnerId) {
        log.error("Could not read Geschaeftspartner {}, sonar.geschaeftspartner.client.url is not set", geschaeftspartnerId);
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, MSG_NOT_REACHABLE);
    }

}
