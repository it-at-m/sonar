package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BerechnungService {

    public void berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId, final BerechnungRequestDTO berechnung) {
        log.debug("Berechnung of Abrechnung {} of Projekt {}: {}", abrechnungId, projektId, berechnung);
    }

}
