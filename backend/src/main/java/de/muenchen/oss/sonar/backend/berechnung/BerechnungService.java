package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungNutzungsobjektRequestDTO;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BerechnungService {

    public void berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId, final BerechnungRequestDTO berechnung) {
        // Prototype: anzahlWochen is a fixed placeholder until the calculation is implemented.
        final int anzahlWochen = 3;

        log.debug("Berechnung of Abrechnung {} of Projekt {} over {} weeks: {}", abrechnungId, projektId, anzahlWochen, berechnung);

        for (int woche = 0; woche < anzahlWochen; woche++) {

            final String gebuehrenstufe = Gebuehrenstufe.getGebuehrenstufe(woche).getBezeichnung();
            log.debug("Berechnung of week {}: {}", woche, gebuehrenstufe);

            final boolean zwischenabrechnungNotwendig = false;
            if (zwischenabrechnungNotwendig) {
                // TODO: create the Zwischenabrechnung for this week.
            }

            for (final BerechnungNutzungsobjektRequestDTO nutzungsobjekt : berechnung.nutzungsobjekte()) {
                log.debug("Berechnung of week {} for Nutzungsobjekt {} at {}", woche, nutzungsobjekt.nutzungsobjektId(),
                        nutzungsobjekt.adressbezeichnung());
            }

        }
    }

}
