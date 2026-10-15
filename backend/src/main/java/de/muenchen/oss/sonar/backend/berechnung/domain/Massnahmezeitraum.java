package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record Massnahmezeitraum(LocalDate beginn, LocalDate ende, int anzahlWochen) {

    public static final LocalDate KEIN_BEGINN = LocalDate.of(3000, 1, 1);
    private static final LocalDate KEIN_ENDE = LocalDate.of(1000, 1, 1);

    public static Massnahmezeitraum from(final List<Flaechenadresse> flaechenadressen,
            final List<Ueberspannungsadresse> ueberspannungsadressen) {

        final LocalDate beginn = Stream.concat(
                flaechenadressen.stream().flatMap(adresse -> adresse.beginn().stream()),
                ueberspannungsadressen.stream().flatMap(adresse -> adresse.earliestStart().stream()))
                .min(Comparator.naturalOrder())
                .orElse(KEIN_BEGINN);

        final LocalDate ende = Stream.concat(
                flaechenadressen.stream().flatMap(adresse -> adresse.ende().stream()),
                ueberspannungsadressen.stream().flatMap(adresse -> adresse.latestEnd().stream()))
                .max(Comparator.naturalOrder())
                .orElse(KEIN_ENDE);

        final int anzahlWochen = flaechenadressen.stream().mapToInt(Flaechenadresse::letzteWoche).reduce(0, Math::max);

        return new Massnahmezeitraum(beginn, ende, anzahlWochen);
    }
}
