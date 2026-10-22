package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.FlaechenadresseService;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.UeberspannungsadresseService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BerechnungslaufService {

    public static Berechnungslauf berechne(final Projekt projekt, final Abrechnung abrechnung,
            final LocalDate berechnungsdatum, final Vorabrechnung vorabrechnung, final String username) {

        final LocalDate beginnWochenlauf = startOfWochenlauf(abrechnung.nutzungsobjekte());

        final List<Flaechenadresse> flaechenadressen = abrechnung.nutzungsobjekte().stream()
                .map(nutzungsobjekt -> FlaechenadresseService.berechne(nutzungsobjekt, berechnungsdatum, beginnWochenlauf))
                .toList();

        final List<Ueberspannungsadresse> ueberspannungsadressen = abrechnung.nutzungsobjekte().stream()
                .map(nutzungsobjekt -> UeberspannungsadresseService.berechne(nutzungsobjekt, berechnungsdatum))
                .filter(adresse -> !adresse.masten().isEmpty())
                .toList();

        return new Berechnungslauf(projekt, username, vorabrechnung, flaechenadressen, ueberspannungsadressen,
                MassnahmezeitraumService.berechne(flaechenadressen, ueberspannungsadressen));
    }

    private static LocalDate startOfWochenlauf(final List<AbrechnungNutzungsobjekt> nutzungsobjekte) {
        return nutzungsobjekte.stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::beginn)
                .min(Comparator.naturalOrder())
                .orElse(MassnahmezeitraumService.KEIN_BEGINN);
    }
}
