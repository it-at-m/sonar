package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.FlaechenabrechnungService;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenadresse;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.FlaechenadresseService;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.UeberspannungsabrechnungService;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsadresse;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.UeberspannungsadresseService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record Berechnungslauf(
        Projekt projekt,
        String username,
        Vorabrechnung vorabrechnung,
        List<Flaechenadresse> flaechenadressen,
        List<Ueberspannungsadresse> ueberspannungsadressen,
        Massnahmezeitraum zeitraum) {

    public Berechnungslauf {
        flaechenadressen = List.copyOf(flaechenadressen);
        ueberspannungsadressen = List.copyOf(ueberspannungsadressen);
    }

    public static Berechnungslauf create(final Projekt projekt, final Abrechnung abrechnung,
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
                Massnahmezeitraum.from(flaechenadressen, ueberspannungsadressen));
    }

    public Berechnungsergebnis run() {

        final Flaechenabrechnung flaechen = FlaechenabrechnungService.berechne(flaechenadressen, zeitraum.anzahlWochen());
        final Ueberspannungsabrechnung ueberspannungen = UeberspannungsabrechnungService.berechne(ueberspannungsadressen);

        final Gebuehren gebuehren = Gebuehren.berechne(flaechen.gebuehr(), ueberspannungen.gebuehr(),
                projekt.verwaltungsgebuehr(), vorabrechnung);

        final int lfdNr = vorabrechnung == null ? 1 : vorabrechnung.lfdNr() + 1;

        final LocalDate abrechnungszeitraumVon = vorabrechnung == null
                ? zeitraum.beginn()
                : vorabrechnung.abrechnungszeitraumBis().plusDays(1);

        return new Berechnungsergebnis(
                lfdNr,
                abrechnungszeitraumVon,
                zeitraum.ende(),
                Zusammenfassung.schreibe(lfdNr, projekt, username, zeitraum.beginn(), zeitraum.ende(),
                        zeitraum.anzahlWochen(), flaechenadressen, ueberspannungsadressen),
                Berechnungslog.schreibe(lfdNr, flaechen, ueberspannungen, gebuehren, vorabrechnung),
                gebuehren.flaechen(),
                gebuehren.ueberspannungen(),
                gebuehren.verwaltung(),
                gebuehren.gesamt(),
                gebuehren.zahlung(),
                flaechen.abschnitte(),
                ueberspannungen.adressen());
    }

    private static LocalDate startOfWochenlauf(final List<AbrechnungNutzungsobjekt> nutzungsobjekte) {
        return nutzungsobjekte.stream()
                .flatMap(nutzungsobjekt -> nutzungsobjekt.positionen().stream())
                .map(AbrechnungPosition::beginn)
                .min(Comparator.naturalOrder())
                .orElse(Massnahmezeitraum.KEIN_BEGINN);
    }
}
