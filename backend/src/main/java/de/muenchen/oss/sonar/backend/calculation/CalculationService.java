package de.muenchen.oss.sonar.backend.calculation;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.calculation.domain.CalculationResult;
import de.muenchen.oss.sonar.backend.calculation.domain.Gebuehren;
import de.muenchen.oss.sonar.backend.calculation.domain.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.calculation.domain.Vorabrechnung;
import de.muenchen.oss.sonar.backend.calculation.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.calculation.ueberspannungen.Ueberspannungsberechnung;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        final Vorabrechnung vorabrechnung = getVorabrechnung(projektId, abrechnungId);

        final Berechnungsgrundlage grundlage = Berechnungsgrundlage.from(abrechnung.nutzungsobjekte(), calculationDate);
        final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(grundlage.flaechenadressen(), grundlage.anzahlWochen());
        final List<Ueberspannungsabrechnung> ueberspannungen = berechneUeberspannungen(grundlage);
        final Gebuehren gebuehren = berechneGebuehren(flaechen, ueberspannungen, projekt, vorabrechnung);

        final CalculationResult ergebnis = erstelleErgebnis(
                projekt, AuthUtils.getUsername(), grundlage, flaechen, ueberspannungen, gebuehren, vorabrechnung);

        log.info("Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.gebuehrFlaechen(), ergebnis.gebuehrUeberspannungen(),
                ergebnis.gebuehrVerwaltung(), ergebnis.gebuehrGesamt());
        return ergebnis;
    }

    private Vorabrechnung getVorabrechnung(final UUID projektId, final UUID abrechnungId) {
        return calculationRepository.findVorabrechnungOfProjekt(projektId, abrechnungId)
                .map(calculation -> new Vorabrechnung(
                        calculation.getLfdNr(), calculation.getAbrechnungszeitraumBis(), calculation.getGebuehrGesamt()))
                .orElse(null);
    }

    private static List<Ueberspannungsabrechnung> berechneUeberspannungen(final Berechnungsgrundlage grundlage) {
        return grundlage.ueberspannungsadressen().stream()
                .map(Ueberspannungsberechnung::berechne)
                .toList();
    }

    private static Gebuehren berechneGebuehren(final Flaechenabrechnung flaechen, final List<Ueberspannungsabrechnung> ueberspannungen,
            final Projekt projekt, final Vorabrechnung vorabrechnung) {

        final BigDecimal gebuehrUeberspannungen = ueberspannungen.stream()
                .map(Ueberspannungsabrechnung::gebuehrUeberspannungen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Gebuehren.berechne(flaechen.gebuehr(), gebuehrUeberspannungen, projekt.verwaltungsgebuehr(), vorabrechnung);
    }

    private static CalculationResult erstelleErgebnis(final Projekt projekt, final String username, final Berechnungsgrundlage grundlage,
            final Flaechenabrechnung flaechen, final List<Ueberspannungsabrechnung> ueberspannungen, final Gebuehren gebuehren,
            final Vorabrechnung vorabrechnung) {

        final int lfdNr = vorabrechnung == null ? 1 : vorabrechnung.lfdNr() + 1;

        final LocalDate abrechnungszeitraumVon = vorabrechnung == null
                ? grundlage.beginnMassnahme()
                : vorabrechnung.abrechnungszeitraumBis().plusDays(1);

        return new CalculationResult(
                lfdNr,
                abrechnungszeitraumVon,
                grundlage.endeMassnahme(),
                Zusammenfassung.schreibe(lfdNr, projekt, username, grundlage),
                Berechnungslog.schreibe(lfdNr, flaechen, ueberspannungen, gebuehren, vorabrechnung),
                gebuehren.flaechen(),
                gebuehren.ueberspannungen(),
                gebuehren.verwaltung(),
                gebuehren.gesamt(),
                gebuehren.zahlung(),
                flaechen.abschnitte(),
                ueberspannungen);
    }
}
