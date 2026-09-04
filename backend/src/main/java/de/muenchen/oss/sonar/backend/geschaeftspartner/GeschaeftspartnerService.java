package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.GeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeschaeftspartnerService {

    private final GeschaeftspartnerClient geschaeftspartnerClient;

    public GeschaeftspartnerResponseDTO getGeschaeftspartner(final String geschaeftspartnerId) {
        log.info("Get Geschaeftspartner with ID {}", geschaeftspartnerId);
        return geschaeftspartnerClient
                .findById(geschaeftspartnerId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, geschaeftspartnerId)));
    }

}
