package de.muenchen.oss.sonar.backend.calculation;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.berechnung.LetzteAbrechnung;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
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
public class CalculationService {

    private final ProjektService projektService;
    private final AbrechnungService abrechnungService;
    private final CalculationRepository calculationRepository;
    private final CalculationEntityMapper calculationEntityMapper;

    @Transactional(readOnly = true)
    public CalculationResult previewCalculation(final UUID projektId, final UUID abrechnungId, final LocalDate calculationDate) {
        return calculate(projektId, abrechnungId, calculationDate);
    }

    @Transactional
    public CalculationResult calculateAndStore(final UUID projektId, final UUID abrechnungId, final LocalDate calculationDate) {
        if (calculationRepository.existsByAbrechnungId(abrechnungId)) {
            throw new ConflictException(String.format(MSG_CALCULATION_ALREADY_EXISTS, abrechnungId));
        }
        final CalculationResult ergebnis = calculate(projektId, abrechnungId, calculationDate);
        calculationRepository.save(calculationEntityMapper.toEntity(ergebnis, abrechnungId));
        log.info("Stored Berechnung {} of Abrechnung {}: Gesamt {}, Zahlung {}",
                ergebnis.lfdNr(), abrechnungId, ergebnis.gebuehrGesamt(), ergebnis.gebuehrZahlung());
        return ergebnis;
    }

    @Transactional(readOnly = true)
    public CalculationResult getCalculation(final UUID projektId, final UUID abrechnungId) {
        if (!abrechnungService.existsAbrechnung(projektId, abrechnungId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId));
        }
        log.info("Get Berechnung of Abrechnung {} of Projekt {}", abrechnungId, projektId);
        return calculationRepository.findByAbrechnungId(abrechnungId)
                .map(calculationEntityMapper::toCalculationResult)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_CALCULATION_NOT_FOUND, abrechnungId)));
    }

    private CalculationResult calculate(final UUID projektId, final UUID abrechnungId, final LocalDate calculationDate) {
        final Projekt projekt = projektService.getProjekt(projektId);
        final Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);
        final LetzteAbrechnung letzteAbrechnung = calculationRepository.findVorabrechnungOfProjekt(projektId, abrechnungId)
                .map(calculation -> new LetzteAbrechnung(calculation.getLfdNr(), calculation.getAbrechnungszeitraumBis(), calculation.getGebuehrGesamt()))
                .orElse(null);

        final CalculationResult ergebnis = new Calculation(
                projekt, abrechnung, AuthUtils.getUsername(), letzteAbrechnung, calculationDate).run();

        log.info("Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.gebuehrFlaechen(), ergebnis.gebuehrUeberspannungen(),
                ergebnis.gebuehrVerwaltung(), ergebnis.gebuehrGesamt());
        return ergebnis;
    }
}
