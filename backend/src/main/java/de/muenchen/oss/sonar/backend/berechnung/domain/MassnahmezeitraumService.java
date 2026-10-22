package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MassnahmezeitraumService {

    public static final LocalDate KEIN_BEGINN = LocalDate.of(3000, 1, 1);
    private static final LocalDate KEIN_ENDE = LocalDate.of(1000, 1, 1);

    public static Massnahmezeitraum berechne(final List<Flaechenadresse> flaechenadressen,
            final List<Ueberspannungsadresse> ueberspannungsadressen) {

        final LocalDate beginn = Stream.concat(
                flaechenadressen.stream().flatMap(adresse -> adresse.beginn().stream()),
                ueberspannungsadressen.stream().flatMap(adresse -> adresse.beginn().stream()))
                .min(Comparator.naturalOrder())
                .orElse(KEIN_BEGINN);

        final LocalDate ende = Stream.concat(
                flaechenadressen.stream().flatMap(adresse -> adresse.ende().stream()),
                ueberspannungsadressen.stream().flatMap(adresse -> adresse.ende().stream()))
                .max(Comparator.naturalOrder())
                .orElse(KEIN_ENDE);

        final int anzahlWochen = flaechenadressen.stream().mapToInt(Flaechenadresse::letzteWoche).reduce(0, Math::max);

        return new Massnahmezeitraum(beginn, ende, anzahlWochen);
    }
}
