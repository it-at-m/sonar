package de.muenchen.oss.sonar.backend.berechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_BERECHNUNG_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_BERECHNUNG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungsergebnisService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungslauf;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungslaufService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Vorabrechnung;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
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
    private final BerechnungRepository berechnungRepository;
    private final BerechnungEntityMapper berechnungEntityMapper;

    @Transactional(readOnly = true)
    public Berechnungsergebnis previewBerechnung(final UUID projektId, final UUID abrechnungId, final LocalDate berechnungsdatum) {
        return calculate(projektId, abrechnungId, berechnungsdatum);
    }

    @Transactional
    public Berechnungsergebnis calculateAndStoreBerechnung(final UUID projektId, final UUID abrechnungId,
            final LocalDate berechnungsdatum) {
        if (berechnungRepository.existsByAbrechnungId(abrechnungId)) {
            throw new ConflictException(String.format(MSG_BERECHNUNG_ALREADY_EXISTS, abrechnungId));
        }
        final Berechnungsergebnis ergebnis = calculate(projektId, abrechnungId, berechnungsdatum);
        berechnungRepository.save(berechnungEntityMapper.toEntity(ergebnis, abrechnungId));
        log.info("Stored Berechnung {} of Abrechnung {}: Gesamt {}, Zahlung {}",
                ergebnis.lfdNr(), abrechnungId, ergebnis.gebuehrGesamt(), ergebnis.gebuehrZahlung());
        return ergebnis;
    }

    @Transactional(readOnly = true)
    public Berechnungsergebnis getBerechnung(final UUID projektId, final UUID abrechnungId) {
        if (!abrechnungService.existsAbrechnung(projektId, abrechnungId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId));
        }
        log.info("Get Berechnung of Abrechnung {} of Projekt {}", abrechnungId, projektId);
        return berechnungRepository.findByAbrechnungId(abrechnungId)
                .map(berechnungEntityMapper::toBerechnungsergebnis)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_BERECHNUNG_NOT_FOUND, abrechnungId)));
    }

    private Berechnungsergebnis calculate(final UUID projektId, final UUID abrechnungId, final LocalDate berechnungsdatum) {

        final Berechnungslauf berechnungslauf = BerechnungslaufService.berechne(
                projektService.getProjekt(projektId),
                abrechnungService.getAbrechnung(projektId, abrechnungId),
                berechnungsdatum,
                getVorabrechnung(projektId, abrechnungId),
                AuthUtils.getUsername());

        final Berechnungsergebnis ergebnis = BerechnungsergebnisService.berechne(berechnungslauf);

        log.info("Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.gebuehrFlaechen(), ergebnis.gebuehrUeberspannungen(),
                ergebnis.gebuehrVerwaltung(), ergebnis.gebuehrGesamt());
        return ergebnis;
    }

    private Vorabrechnung getVorabrechnung(final UUID projektId, final UUID abrechnungId) {
        return berechnungRepository.findVorabrechnungOfProjekt(projektId, abrechnungId)
                .map(berechnung -> new Vorabrechnung(
                        berechnung.getLfdNr(), berechnung.getAbrechnungszeitraumBis(), berechnung.getGebuehrGesamt()))
                .orElse(null);
    }
}
