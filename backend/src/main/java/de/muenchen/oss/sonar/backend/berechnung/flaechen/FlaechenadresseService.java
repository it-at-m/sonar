package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaechenadresseService {

    public static Flaechenadresse from(final AbrechnungNutzungsobjekt nutzungsobjekt, final LocalDate berechnungsdatum,
            final LocalDate beginnWochenlauf) {

        final List<AbrechnungPosition> sortedByBeginn = nutzungsobjekt.positionen().stream()
                .sorted(Comparator.comparing(AbrechnungPosition::beginn))
                .toList();

        final List<Flaeche> flaechen = new ArrayList<>();
        BigDecimal gesamtflaecheVorher = BigDecimal.ZERO;

        for (final AbrechnungPosition position : sortedByBeginn) {
            final Flaeche flaeche = FlaecheService.from(position, flaechen.size() + 1, berechnungsdatum, beginnWochenlauf,
                    gesamtflaecheVorher);
            flaechen.add(flaeche);
            gesamtflaecheVorher = flaeche.gesamtflaecheAdresse();
        }

        final int letzteWoche = flaechen.stream().mapToInt(Flaeche::letzteWoche).max().orElse(0);

        return new Flaechenadresse(nutzungsobjekt.adressbezeichnung(), nutzungsobjekt.aufschlag50prozent(), flaechen, letzteWoche);
    }

    public static List<Flaechenbeitrag> beitraege(final Flaechenadresse adresse, final int woche) {

        final List<Flaeche> flaechen = adresse.flaechen();
        final BigDecimal[] beitraegeQm = flaechen.stream().map(Flaeche::flaechenaenderung).toArray(BigDecimal[]::new);

        for (int index = 0; index < flaechen.size(); index++) {
            if (FlaecheService.hasTakenEffectBy(flaechen.get(index), woche) && beitraegeQm[index].signum() < 0) {
                reduziere(beitraegeQm, index);
            }
        }

        final List<Flaechenbeitrag> beitraege = new ArrayList<>(flaechen.size());
        for (int index = 0; index < flaechen.size(); index++) {
            beitraege.add(new Flaechenbeitrag(flaechen.get(index), beitraegeQm[index], woche, adresse.letzteWoche()));
        }
        return beitraege;
    }

    private static void reduziere(final BigDecimal[] beitraegeQm, final int reduzierende) {

        BigDecimal rest = beitraegeQm[reduzierende].abs();
        beitraegeQm[reduzierende] = BigDecimal.ZERO;

        for (int index = 0; index < beitraegeQm.length && rest.signum() > 0; index++) {

            if (beitraegeQm[index].signum() == 0) {
                continue;
            }

            if (rest.compareTo(beitraegeQm[index]) > 0) {
                rest = rest.subtract(beitraegeQm[index]);
                beitraegeQm[index] = BigDecimal.ZERO;
            } else {
                beitraegeQm[index] = beitraegeQm[index].subtract(rest);
                rest = BigDecimal.ZERO;
            }
        }
    }
}
