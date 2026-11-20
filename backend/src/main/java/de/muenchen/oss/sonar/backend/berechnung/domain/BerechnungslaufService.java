package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.FlaechenadresseService;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.VorgaengerPosition;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.UeberspannungsadresseService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BerechnungslaufService {

    public static Berechnungslauf berechne(final Projekt projekt, final Abrechnung abrechnung, final List<VorgaengerProjekt> vorgaengerProjekte,
            final LocalDate berechnungsdatum, final Vorabrechnung vorabrechnung, final String username) {

        final List<AbrechnungNutzungsobjekt> vorgaengerNutzungsobjekte = vorgaengerProjekte.stream()
                .flatMap(vorgaengerProjekt -> vorgaengerProjekt.nutzungsobjekte().stream())
                .toList();

        final LocalDate beginnWochenlauf = startOfWochenlauf(concat(abrechnung.nutzungsobjekte(), vorgaengerNutzungsobjekte));

        final Set<UUID> eigeneNutzungsobjektIds = abrechnung.nutzungsobjekte().stream()
                .map(AbrechnungNutzungsobjekt::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final Map<UUID, List<VorgaengerPosition>> vorgaengerPositionen = vorgaengerPositionenOf(vorgaengerProjekte, eigeneNutzungsobjektIds);

        final List<Flaechenadresse> flaechenadressen = abrechnung.nutzungsobjekte().stream()
                .map(nutzungsobjekt -> FlaechenadresseService.berechne(nutzungsobjekt,
                        vorgaengerPositionen.getOrDefault(nutzungsobjekt.id(), List.of()), berechnungsdatum, beginnWochenlauf))
                .toList();

        final List<Ueberspannungsadresse> ueberspannungsadressen = adressenMitMasten(abrechnung.nutzungsobjekte(), berechnungsdatum);

        final List<Flaechenadresse> vorgaengerFlaechenadressen = vorgaengerProjekte.stream()
                .flatMap(vorgaengerProjekt -> vorgaengerProjekt.nutzungsobjekte().stream()
                        .filter(nutzungsobjekt -> !eigeneNutzungsobjektIds.contains(nutzungsobjekt.id()))
                        .map(nutzungsobjekt -> mitProjektnummer(vorgaengerProjekt.projektnummer(),
                                FlaechenadresseService.berechne(nutzungsobjekt, List.of(), berechnungsdatum, beginnWochenlauf))))
                .toList();

        // Überspannungen are charged per Adresse, so the Masten of the Vorgaenger only stretch the Zeitraum.
        final List<Ueberspannungsadresse> vorgaengerUeberspannungsadressen = adressenMitMasten(vorgaengerNutzungsobjekte, berechnungsdatum);

        final Massnahmezeitraum zeitraum = MassnahmezeitraumService.berechne(
                concat(flaechenadressen, vorgaengerFlaechenadressen),
                concat(ueberspannungsadressen, vorgaengerUeberspannungsadressen));

        return new Berechnungslauf(projekt, username, vorabrechnung, flaechenadressen, ueberspannungsadressen,
                vorgaengerFlaechenadressen, zeitraum);
    }

    /**
     * A Nutzungsobjekt that the calculated Abrechnung took over from a Vorgaenger stays one Adresse,
     * so the Flächen of that Vorgaenger keep their age in it.
     */
    private static Map<UUID, List<VorgaengerPosition>> vorgaengerPositionenOf(final List<VorgaengerProjekt> vorgaengerProjekte,
            final Set<UUID> eigeneNutzungsobjektIds) {
        return vorgaengerProjekte.stream()
                .flatMap(vorgaengerProjekt -> vorgaengerProjekt.nutzungsobjekte().stream()
                        .filter(nutzungsobjekt -> eigeneNutzungsobjektIds.contains(nutzungsobjekt.id()))
                        .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream()
                                .map(position -> Map.entry(nutzungsobjekt.id(),
                                        new VorgaengerPosition(vorgaengerProjekt.projektnummer(), position)))))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private static List<Ueberspannungsadresse> adressenMitMasten(final List<AbrechnungNutzungsobjekt> nutzungsobjekte,
            final LocalDate berechnungsdatum) {
        return nutzungsobjekte.stream()
                .map(nutzungsobjekt -> UeberspannungsadresseService.berechne(nutzungsobjekt, berechnungsdatum))
                .filter(adresse -> !adresse.masten().isEmpty())
                .toList();
    }

    private static Flaechenadresse mitProjektnummer(final String projektnummer, final Flaechenadresse adresse) {
        return new Flaechenadresse("Projekt " + projektnummer + ", " + adresse.adressbezeichnung(), adresse.isAufschlag50Prozent(),
                adresse.flaechen(), adresse.letzteWoche());
    }

    private static <T> List<T> concat(final List<T> eigene, final List<T> vorgaenger) {
        return Stream.concat(eigene.stream(), vorgaenger.stream()).toList();
    }

    private static LocalDate startOfWochenlauf(final List<AbrechnungNutzungsobjekt> nutzungsobjekte) {
        return nutzungsobjekte.stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::beginn)
                .min(Comparator.naturalOrder())
                .orElse(MassnahmezeitraumService.KEIN_BEGINN);
    }
}
