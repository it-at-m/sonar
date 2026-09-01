package de.muenchen.oss.sonar.backend.geschaeftspartner;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;

public interface GeschaeftspartnerClient {

    String MSG_NOT_REACHABLE = "Die Geschäftspartnerdaten konnten nicht abgerufen werden.";

    Optional<GeschaeftspartnerResponseDTO> findById(String geschaeftspartnerId);

}
