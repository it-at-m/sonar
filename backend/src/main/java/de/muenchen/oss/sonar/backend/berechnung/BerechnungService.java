package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnung;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BerechnungService {

    // TODO: fixed placeholder until the Verwaltungsgebuehr is calculated.
    private static final BigDecimal VERWALTUNGSGEBUEHR = new BigDecimal("25");

    private final AbrechnungService abrechnungService;

    public Berechnung berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId, final BerechnungRequestDTO berechnungRequestDTO) {
        final Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);
        final List<AbrechnungPosition> positionen = abrechnung.nutzungsobjekte().stream()
                .map(AbrechnungNutzungsobjekt::positionen)
                .flatMap(List::stream)
                .toList();

        final Berechnung berechnung = new Berechnung();
        berechnung.setGebuehrVerwaltung(VERWALTUNGSGEBUEHR);
        berechnung.setGebuehrNutzung(BigDecimal.ZERO);
        if (positionen.isEmpty()) {
            log.debug("Berechnung of Abrechnung {} of Projekt {}: no Position to charge", abrechnungId, projektId);
            return berechnung;
        }

        final LocalDate beginnMassnahme = positionen.stream().map(AbrechnungPosition::beginn).min(Comparator.naturalOrder()).orElseThrow();
        final LocalDate endeMassnahme = positionen.stream().map(AbrechnungPosition::ende).max(Comparator.naturalOrder()).orElseThrow();
        berechnung.setAbrechnungszeitraumVon(beginnMassnahme);
        berechnung.setAbrechnungszeitraumBis(endeMassnahme);
        log.debug("Berechnung of Abrechnung {}: Massnahme from {} to {}", abrechnungId, beginnMassnahme, endeMassnahme);

        BigDecimal gebuehrNutzung = BigDecimal.ZERO;
        for (final Berechnungswoche woche : Berechnungswoche.weeksOf(beginnMassnahme, endeMassnahme)) {
            // TODO: check whether this week makes a Zwischenabrechnung necessary, and create it.
            gebuehrNutzung = gebuehrNutzung.add(gebuehrDerWoche(woche, positionen));
        }
        berechnung.setGebuehrNutzung(gebuehrNutzung);

        log.debug("Berechnung of Abrechnung {} of Projekt {}: Gebuehren of {}", abrechnungId, projektId, gebuehrNutzung);
        return berechnung;
    }

    private BigDecimal gebuehrDerWoche(final Berechnungswoche woche, final List<AbrechnungPosition> positionen) {
        final List<AbrechnungPosition> activePositionen = positionen.stream()
                .filter(position -> Zeitraum.overlap(position.beginn(), position.ende(), woche.beginn(), woche.ende()))
                .toList();

        // The Gebuehrenstufe of the size follows from everything in use that week across the whole
        // Abrechnung, while the Stufe of the duration is counted per Position.
        int gesamtflaecheQm = 0;
        for (final AbrechnungPosition position : activePositionen) {
            gesamtflaecheQm += position.getFlaecheQmRoundedUp();
        }

        BigDecimal gebuehrWoche = BigDecimal.ZERO;
        for (final AbrechnungPosition position : activePositionen) {
            final int flaecheQm = position.getFlaecheQmRoundedUp();
            final int laufendeWoche = position.getLaufendeWoche(woche.beginn());
            // TODO: der Aufschlag liegt aktuell bei der Position, soll aber bei der Adresse liegen. Diese
            // Eigenschaft liegt bei der Adresse (Nutzungsobjekt) und gilt fuer alle dazugehoerigen
            // Flaechen (Positionen).
            final BigDecimal gebuehrProQm = Gebuehrenstufe.getGebuehrProQm(laufendeWoche, gesamtflaecheQm, position.aufschlag50prozent());
            log.debug("Berechnung of week {}: {} qm of {} qm in laufende Woche {} at {} per qm",
                    woche.nummer(), flaecheQm, gesamtflaecheQm, laufendeWoche, gebuehrProQm);
            gebuehrWoche = gebuehrWoche.add(gebuehrProQm.multiply(BigDecimal.valueOf(flaecheQm)));
        }
        return gebuehrWoche;
    }

}
