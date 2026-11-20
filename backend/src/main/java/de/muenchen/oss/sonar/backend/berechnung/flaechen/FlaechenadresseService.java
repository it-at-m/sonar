package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaechenadresseService {

    public static Flaechenadresse berechne(final AbrechnungNutzungsobjekt nutzungsobjekt, final List<VorgaengerPosition> vorgaengerPositionen,
            final LocalDate berechnungsdatum, final LocalDate beginnWochenlauf) {

        final List<Adressposition> sortedByBeginn = Stream.concat(
                vorgaengerPositionen.stream().map(vorgaengerPosition -> new Adressposition(vorgaengerPosition.position(),
                        " aus Projekt " + vorgaengerPosition.projektnummer(), true)),
                nutzungsobjekt.positionen().stream().map(position -> new Adressposition(position, "", false)))
                // On the same beginn the own Position comes last, so that it governs the Woche.
                .sorted(Comparator.comparing((Adressposition adressposition) -> adressposition.position().beginn())
                        .thenComparing(Adressposition::isFromOtherProjekt, Comparator.reverseOrder()))
                .toList();

        final List<Flaeche> adressflaechen = new ArrayList<>();
        BigDecimal gesamtflaecheVorher = BigDecimal.ZERO;

        for (final Adressposition adressposition : sortedByBeginn) {
            final Flaeche flaeche = FlaecheService.berechne(adressposition.position(),
                    "Position " + (adressflaechen.size() + 1) + adressposition.herkunft(), adressposition.isFromOtherProjekt(),
                    berechnungsdatum, beginnWochenlauf, gesamtflaecheVorher);
            adressflaechen.add(flaeche);
            gesamtflaecheVorher = flaeche.gesamtflaecheAdresse();
        }

        final int letzteWoche = adressflaechen.stream().mapToInt(Flaeche::letzteWoche).max().orElse(0);

        return new Flaechenadresse(nutzungsobjekt.adressbezeichnung(), nutzungsobjekt.aufschlag50prozent(), adressflaechen,
                letzteWoche);
    }

    public static boolean isChargedInOtherProjekt(final Flaechenadresse adresse, final int woche) {
        boolean isChargedInOtherProjekt = false;
        for (final Flaeche flaeche : adresse.flaechen()) {
            if (FlaecheService.hasTakenEffectBy(flaeche, woche)) {
                isChargedInOtherProjekt = flaeche.isFromOtherProjekt();
            }
        }
        return isChargedInOtherProjekt;
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

    private record Adressposition(AbrechnungPosition position, String herkunft, boolean isFromOtherProjekt) {
    }
}
