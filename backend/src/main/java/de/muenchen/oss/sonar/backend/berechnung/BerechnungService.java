package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungsergebnisService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungslauf;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungslaufService;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BerechnungService {

    private final ProjektService projektService;
    private final AbrechnungService abrechnungService;

    @Transactional(readOnly = true)
    public Berechnungsergebnis previewBerechnung(final UUID projektId, final UUID abrechnungId, final LocalDate berechnungsdatum) {

        final Berechnungslauf berechnungslauf = BerechnungslaufService.berechne(
                projektService.getProjekt(projektId),
                abrechnungService.getAbrechnung(projektId, abrechnungId),
                berechnungsdatum,
                null,
                AuthUtils.getUsername());

        final Berechnungsergebnis ergebnis = BerechnungsergebnisService.berechne(berechnungslauf);

        log.info("Preview Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.gebuehrFlaechen(), ergebnis.gebuehrUeberspannungen(),
                ergebnis.gebuehrVerwaltung(), ergebnis.gebuehrGesamt());
        return ergebnis;
    }
}
