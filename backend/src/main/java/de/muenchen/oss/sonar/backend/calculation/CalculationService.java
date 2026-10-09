package de.muenchen.oss.sonar.backend.calculation;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_CALCULATION_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.calculation.domain.CalculationResult;
import de.muenchen.oss.sonar.backend.calculation.domain.Gebuehren;
import de.muenchen.oss.sonar.backend.calculation.domain.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.calculation.domain.Vorabrechnung;
import de.muenchen.oss.sonar.backend.calculation.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.calculation.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.calculation.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.calculation.ueberspannungen.Ueberspannungsberechnung;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculationService {

    private static final LocalDate KEIN_BEGINN = LocalDate.of(3000, 1, 1);
    private static final LocalDate KEIN_ENDE = LocalDate.of(2000, 1, 1);

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

        final LocalDate beginnWochenlauf = beginnWochenlauf(abrechnung.nutzungsobjekte());

        final List<Flaechenadresse> alleAdressen = abrechnung.nutzungsobjekte().stream()
                .map(nutzungsobjekt -> Flaechenadresse.from(nutzungsobjekt, calculationDate, beginnWochenlauf))
                .toList();
        final List<Ueberspannungsadresse> adressenMitMasten = abrechnung.nutzungsobjekte().stream()
                .map(Ueberspannungsadresse::from)
                .filter(adresse -> !adresse.masten().isEmpty())
                .toList();

        final Flaechenabrechnung flaechen = Flaechenabrechnung.berechne(alleAdressen, anzahlWochen(alleAdressen));
        final List<Ueberspannungsabrechnung> ueberspannungen = berechneUeberspannungen(adressenMitMasten);
        final Gebuehren gebuehren = berechneGebuehren(flaechen, ueberspannungen, projekt, vorabrechnung);

        final CalculationResult ergebnis = erstelleErgebnis(
                projekt, AuthUtils.getUsername(), alleAdressen, adressenMitMasten, flaechen, ueberspannungen, gebuehren, vorabrechnung);

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

    private static List<Ueberspannungsabrechnung> berechneUeberspannungen(final List<Ueberspannungsadresse> adressenMitMasten) {
        return adressenMitMasten.stream()
                .map(Ueberspannungsberechnung::berechne)
                .toList();
    }

    private static LocalDate beginnWochenlauf(final List<AbrechnungNutzungsobjekt> nutzungsobjekte) {
        return nutzungsobjekte.stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::beginn)
                .min(Comparator.naturalOrder())
                .orElse(KEIN_BEGINN);
    }

    private static int anzahlWochen(final List<Flaechenadresse> alleAdressen) {
        return alleAdressen.stream().mapToInt(Flaechenadresse::letzteWoche).reduce(0, Math::max);
    }

    private static LocalDate beginnMassnahme(final List<Flaechenadresse> alleAdressen,
            final List<Ueberspannungsadresse> adressenMitMasten) {
        return Stream.concat(
                alleAdressen.stream().flatMap(adresse -> adresse.beginn().stream()),
                adressenMitMasten.stream().flatMap(adresse -> adresse.beginn().stream()))
                .min(Comparator.naturalOrder())
                .orElse(KEIN_BEGINN);
    }

    private static LocalDate endeMassnahme(final List<Flaechenadresse> alleAdressen,
            final List<Ueberspannungsadresse> adressenMitMasten) {
        return Stream.concat(
                alleAdressen.stream().flatMap(adresse -> adresse.ende().stream()),
                adressenMitMasten.stream().flatMap(adresse -> adresse.ende().stream()))
                .max(Comparator.naturalOrder())
                .orElse(KEIN_ENDE);
    }

    private static Gebuehren berechneGebuehren(final Flaechenabrechnung flaechen, final List<Ueberspannungsabrechnung> ueberspannungen,
            final Projekt projekt, final Vorabrechnung vorabrechnung) {

        final BigDecimal gebuehrUeberspannungen = ueberspannungen.stream()
                .map(Ueberspannungsabrechnung::gebuehrUeberspannungen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Gebuehren.berechne(flaechen.gebuehr(), gebuehrUeberspannungen, projekt.verwaltungsgebuehr(), vorabrechnung);
    }

    private static CalculationResult erstelleErgebnis(final Projekt projekt, final String username,
            final List<Flaechenadresse> alleAdressen, final List<Ueberspannungsadresse> adressenMitMasten,
            final Flaechenabrechnung flaechen, final List<Ueberspannungsabrechnung> ueberspannungen, final Gebuehren gebuehren,
            final Vorabrechnung vorabrechnung) {

        final int lfdNr = vorabrechnung == null ? 1 : vorabrechnung.lfdNr() + 1;

        final LocalDate beginnMassnahme = beginnMassnahme(alleAdressen, adressenMitMasten);
        final LocalDate endeMassnahme = endeMassnahme(alleAdressen, adressenMitMasten);

        final LocalDate abrechnungszeitraumVon = vorabrechnung == null
                ? beginnMassnahme
                : vorabrechnung.abrechnungszeitraumBis().plusDays(1);

        return new CalculationResult(
                lfdNr,
                abrechnungszeitraumVon,
                endeMassnahme,
                Zusammenfassung.schreibe(lfdNr, projekt, username, beginnMassnahme, endeMassnahme,
                        anzahlWochen(alleAdressen), alleAdressen, adressenMitMasten),
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
