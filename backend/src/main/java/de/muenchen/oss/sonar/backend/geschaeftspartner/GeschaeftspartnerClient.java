package de.muenchen.oss.sonar.backend.geschaeftspartner;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import java.util.Optional;

public interface GeschaeftspartnerClient {

    Optional<GeschaeftspartnerResponseDTO> findById(String geschaeftspartnerId);

}
