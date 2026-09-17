package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BerechnungService {

    private final AbrechnungService abrechnungService;

    public void berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId, final BerechnungRequestDTO berechnung) {
        // Prototype: anzahlWochen is a fixed placeholder until the calculation is implemented.
        final int anzahlWochen = 3;

        final Map<Boolean, Map<String, BigDecimal>> flaecheProZeitindex = new HashMap<>();
        flaecheProZeitindex.put(true, new HashMap<>());
        flaecheProZeitindex.put(false, new HashMap<>());

        final Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);

        for (int woche = 0; woche < anzahlWochen; woche++) {

            final String gebuehrenstufe = Gebuehrenstufe.getGebuehrenstufe(woche).getBezeichnung();
            log.debug("Berechnung of week {}: {}", woche, gebuehrenstufe);

            final boolean zwischenabrechnungNotwendig = false;
            if (zwischenabrechnungNotwendig) {
                // TODO: create the Zwischenabrechnung for this week.
            }

            for (final AbrechnungNutzungsobjekt nutzungsobjekt : abrechnung.nutzungsobjekte()) {

                final ResultVerteilung resultVerteilung = nutzungsobjekt.getFlaecheProZeitindex(woche);
                for (final Map.Entry<String, BigDecimal> proZeitindex : resultVerteilung.getFlaecheQmProZeitindex().entrySet()) {

                    final String zeitindex = proZeitindex.getKey();
                    final BigDecimal flaecheQm = proZeitindex.getValue();

                    // Prototype: everything goes into the false bucket until the Aufschlag is decided per
                    // Position.
                    flaecheProZeitindex.get(false).merge(zeitindex, flaecheQm, BigDecimal::add);
                }
            }

        }
    }

}
