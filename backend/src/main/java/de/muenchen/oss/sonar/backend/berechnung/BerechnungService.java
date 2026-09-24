package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.abrechnung.Abrechnung;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Runs the calculation that was built as a prototype. The prototype received everything through the
 * request body. Here the Projekt and the Abrechnung are read by their id, and their stored
 * Nutzungsobjekte and Positionen are handed to the ported classes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BerechnungService {

    // TODO: fixed placeholder until the Verwaltungsgebuehr is calculated.
    private static final BigDecimal VERWALTUNGSGEBUEHR = new BigDecimal("25");

    private final ProjektService projektService;
    private final AbrechnungService abrechnungService;

    @Transactional(readOnly = true)
    public Abrechnung berechnungDurchfuehren(final UUID projektId, final UUID abrechnungId) {
        final Projekt projekt = projektService.getProjekt(projektId);
        final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);

        final Bauprojekt bauprojekt = createBauprojekt(projekt, abrechnung);
        bauprojekt.setUsername(AuthUtils.getUsername());
        // The AbrechnungEntity stores no Gebuehren, so no Vorgaenger can be handed in. The lfdNr stays 1
        // and the Zahlbetrag equals the Gesamtbetrag until the Gebuehren of an Abrechnung are persisted.
        bauprojekt.setLetzteAbrechnung(null);

        final Abrechnung ergebnis = bauprojekt.abrechnungErstellen();
        log.info("Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.getGebuehrFlaechen(), ergebnis.getGebuehrUeberspannungen(),
                ergebnis.getGebuehrVerwaltung(), ergebnis.getGebuehrGesamt());
        log.debug("Zusammenfassung der Abrechnung {}:{}{}", abrechnungId, System.lineSeparator(), ergebnis.getZusammenfassung());
        log.debug("Berechnungslog der Abrechnung {}:{}{}", abrechnungId, System.lineSeparator(), ergebnis.getBerechnungslog());
        return ergebnis;
    }

    /**
     * Builds the same objects the prototype built from its request body, and in the same order.
     */
    private Bauprojekt createBauprojekt(final Projekt projekt, final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung) {

        Bauprojekt bp = null;

        // The model holds no Projektname, so the Projektnummer stands in for both.
        final String projektName = projekt.projektnummer();
        final String projektNummer = projekt.projektnummer();
        final LocalDate abrechnungsdatumEnde = abrechnung.zeitraumBis();

        int adressId = 0;

        for (final AbrechnungNutzungsobjekt nutzungsobjekt : abrechnung.nutzungsobjekte()) {

            final String adressname = adressbezeichnung(nutzungsobjekt);
            // The Aufschlag belongs to the Nutzungsobjekt and holds for all of its Positionen, but the
            // schema keeps it on the Position. The first one decides for the whole Adresse until the flag
            // has moved to the Nutzungsobjekt.
            final Boolean aufschlag50prozent = nutzungsobjekt.positionen().getFirst().haelfte();

            if (bp == null) {
                // Erster Lauf: Bauprojekt erstellen
                bp = new Bauprojekt(projektName, projektNummer, new Adresse(adressId, adressname, aufschlag50prozent));
            } else {
                // Adresse hinzufügen
                bp.addAdresse(new Adresse(adressId, adressname, aufschlag50prozent));
            }

            // A Flaeche carries the total area of its Adresse from that date on, and the calculation
            // subtracts the entry before it. The running sum is what makes that subtraction yield the
            // Position's own Flaeche. It also means a Position keeps being charged until the last week of
            // its Nutzungsobjekt, whatever its own Ende says, because Flaeche.getZeitindex deactivates on
            // the last week of the Adresse and not on the one of the Flaeche.
            BigDecimal flaecheQmKumuliert = BigDecimal.ZERO;
            int flaechenNummer = 0;

            for (final AbrechnungPosition position : positionenNachBeginn(nutzungsobjekt)) {

                flaechenNummer++;
                // A Position carries no name of its own.
                final String flaechenname = "Position " + flaechenNummer;
                final LocalDate gueltigVon = position.beginn();
                final LocalDate gueltigBis;

                // Fläche darf nicht länger gültig sein für Berechnung als AbrechnungsdatumEnde
                if (position.ende().compareTo(abrechnungsdatumEnde) > 0) {
                    gueltigBis = abrechnungsdatumEnde;
                } else {
                    gueltigBis = position.ende();
                }
                flaecheQmKumuliert = flaecheQmKumuliert.add(position.flaeche());

                bp.addFlaeche(adressId, new Flaeche(flaechenname, gueltigVon, gueltigBis, flaecheQmKumuliert));
            }

            adressId++;
        }

        // The prototype also calculated Ueberspannungen. The model holds no Masten, so that half stays
        // empty and contributes nothing.

        bp.setVerwaltungsgebuehren(VERWALTUNGSGEBUEHR);

        bp.aktualisiereDaten();

        return bp;
    }

    /**
     * The calculation reads the Flaechen of an Adresse as a sequence in time, so the Positionen are
     * ordered by their Beginn. The sort is stable, so Positionen starting on the same day keep the
     * order they were entered in.
     */
    private List<AbrechnungPosition> positionenNachBeginn(final AbrechnungNutzungsobjekt nutzungsobjekt) {
        return nutzungsobjekt.positionen().stream()
                .sorted(Comparator.comparing(AbrechnungPosition::beginn))
                .toList();
    }

    /**
     * The Art decides how the line reads, so it is either "Musterstraße 1–3" or "Flurstück 123,
     * Gemarkung".
     */
    private String adressbezeichnung(final AbrechnungNutzungsobjekt nutzungsobjekt) {
        if (nutzungsobjekt.art() == Adressart.FLURSTUECK) {
            return "Flurstück " + joinNonBlank(", ", nutzungsobjekt.flurstueck(), nutzungsobjekt.gemarkung());
        }
        final String hausnummer = StringUtils.hasText(nutzungsobjekt.hausnummerBis())
                ? nutzungsobjekt.hausnummerVon() + "–" + nutzungsobjekt.hausnummerBis()
                : nutzungsobjekt.hausnummerVon();
        return joinNonBlank(" ", nutzungsobjekt.adresse(), hausnummer);
    }

    private String joinNonBlank(final String separator, final String... teile) {
        return Arrays.stream(teile).filter(StringUtils::hasText).collect(Collectors.joining(separator));
    }

}
