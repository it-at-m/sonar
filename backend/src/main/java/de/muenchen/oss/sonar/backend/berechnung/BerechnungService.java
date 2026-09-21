package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnung;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
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

    public Berechnung berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId, final BerechnungRequestDTO berechnungRequestDTO) {
        // Prototype: anzahlWochen is a fixed placeholder until the calculation is implemented.
        final int anzahlWochen = 3;

        final Berechnung berechnung = new Berechnung();

        final Map<Boolean, Map<String, BigDecimal>> flaecheProZeitindex = new HashMap<>();
        flaecheProZeitindex.put(true, new HashMap<>());
        flaecheProZeitindex.put(false, new HashMap<>());

        final Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);

        final LocalDate beginnMassnahme = abrechnung.nutzungsobjekte().stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::beginn)
                .min(Comparator.naturalOrder())
                .orElse(null);
        final LocalDate endeMassnahme = abrechnung.nutzungsobjekte().stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::ende)
                .max(Comparator.naturalOrder())
                .orElse(null);
        log.debug("Berechnung of Abrechnung {}: Massnahme from {} to {}", abrechnungId, beginnMassnahme, endeMassnahme);

        BigDecimal gebuehrenGesamtFlaeche = BigDecimal.ZERO;

        for (int woche = 1; woche <= anzahlWochen; woche++) {

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

                    // TODO: aufschlag ist aktuell haelfte bei nutzungsobjekt, soll aber aufschlag bei
                    // adresse sein. Diese Eigenschaft liegt bei der Adresse (Nutzungsobjekt) und
                    // gilt fuer alle dazugehoerigen Flaechen (Positionen).
                    final boolean aufschlag = nutzungsobjekt.positionen().getFirst().haelfte();

                    flaecheProZeitindex.get(aufschlag).merge(zeitindex, flaecheQm, BigDecimal::add);
                }
            }

            BigDecimal gebuehrenWochensumme = BigDecimal.ZERO;

            for (final Map.Entry<Boolean, Map<String, BigDecimal>> proAufschlag : flaecheProZeitindex.entrySet()) {

                final boolean aufschlag50prozent = proAufschlag.getKey();
                final Map<String, BigDecimal> mapInnen = proAufschlag.getValue();

                for (final Map.Entry<String, BigDecimal> proZeitindex : mapInnen.entrySet()) {

                    final String zeitindex = proZeitindex.getKey();
                    final BigDecimal flaecheQm = proZeitindex.getValue();

                    final int flaecheGerundet = flaecheQm.setScale(0, RoundingMode.CEILING).intValueExact();
                    log.debug("Berechnung of week {}: {} qm in {} with Aufschlag {}", woche, flaecheGerundet, zeitindex,
                            aufschlag50prozent);

                    final BigDecimal gebuehrensatz = Gebuehrenstufe.getGebuehrensatz(aufschlag50prozent, gebuehrenstufe, zeitindex);
                    final BigDecimal gebuehr = gebuehrensatz.multiply(BigDecimal.valueOf(flaecheGerundet));
                    log.debug("Berechnung of week {}: {} at a Gebuehrensatz of {} makes a Gebuehr of {}", woche, gebuehrenstufe,
                            gebuehrensatz, gebuehr);

                    if (gebuehr.signum() != 0) {
                        gebuehrenWochensumme = gebuehrenWochensumme.add(gebuehr);
                    }
                }
            }

            log.debug("Berechnung of week {}: Gebuehren of {}", woche, gebuehrenWochensumme);

            gebuehrenGesamtFlaeche = gebuehrenGesamtFlaeche.add(gebuehrenWochensumme);
        }

        for (final Map.Entry<Boolean, Map<String, BigDecimal>> proAufschlag : flaecheProZeitindex.entrySet()) {

            final boolean aufschlag50prozent = proAufschlag.getKey();
            final Map<String, BigDecimal> flaechenverteilung = proAufschlag.getValue();

            for (final Map.Entry<String, BigDecimal> proZeitindex : flaechenverteilung.entrySet()) {

                final String zeitindex = proZeitindex.getKey();
                final BigDecimal flaecheQm = proZeitindex.getValue();

                final int flaecheGerundet = flaecheQm.setScale(0, RoundingMode.CEILING).intValueExact();
                final String gebuehrenstufe = Gebuehrenstufe.getGebuehrenstufe(flaecheGerundet).getBezeichnung();
                final BigDecimal gebuehrensatz = Gebuehrenstufe.getGebuehrensatz(aufschlag50prozent, gebuehrenstufe, zeitindex);
                final BigDecimal gebuehr = gebuehrensatz.multiply(BigDecimal.valueOf(flaecheGerundet));

                log.debug("Berechnung of Abrechnung {}: {} qm in {} with Aufschlag {} in {} at {} makes a Gebuehr of {}", abrechnungId,
                        flaecheGerundet, zeitindex, aufschlag50prozent, gebuehrenstufe, gebuehrensatz, gebuehr);
            }
        }

        log.debug("Berechnung of Abrechnung {} of Projekt {}: Gebuehren of {}", abrechnungId, projektId, gebuehrenGesamtFlaeche);

        return berechnung;
    }

}
