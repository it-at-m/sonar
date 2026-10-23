package de.muenchen.oss.sonar.backend.berechnung.domain;

import de.muenchen.oss.sonar.backend.berechnung.flaechen.Flaechenabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.FlaechenabrechnungService;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.Ueberspannungsabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.ueberspannungen.UeberspannungsabrechnungService;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BerechnungsergebnisService {

    public static Berechnungsergebnis berechne(final Berechnungslauf berechnungslauf) {

        final Massnahmezeitraum zeitraum = berechnungslauf.zeitraum();
        final Vorabrechnung vorabrechnung = berechnungslauf.vorabrechnung();

        final Flaechenabrechnung flaechen = FlaechenabrechnungService.berechne(berechnungslauf.flaechenadressen(),
                zeitraum.anzahlWochen());
        final Ueberspannungsabrechnung ueberspannungen = UeberspannungsabrechnungService.berechne(
                berechnungslauf.ueberspannungsadressen());

        final Gebuehren gebuehren = GebuehrenService.berechne(flaechen.gebuehr(), ueberspannungen.gebuehr(),
                berechnungslauf.projekt().verwaltungsgebuehr(), vorabrechnung);

        final int lfdNr = vorabrechnung == null ? 1 : vorabrechnung.lfdNr() + 1;

        final LocalDate abrechnungszeitraumVon = vorabrechnung == null
                ? zeitraum.beginn()
                : vorabrechnung.abrechnungszeitraumBis().plusDays(1);

        return new Berechnungsergebnis(
                lfdNr,
                abrechnungszeitraumVon,
                zeitraum.ende(),
                Zusammenfassung.schreibe(lfdNr, berechnungslauf.projekt(), berechnungslauf.username(), zeitraum.beginn(),
                        zeitraum.ende(), zeitraum.anzahlWochen(), berechnungslauf.flaechenadressen(),
                        berechnungslauf.ueberspannungsadressen()),
                Berechnungslog.schreibe(lfdNr, flaechen, ueberspannungen, gebuehren, vorabrechnung),
                gebuehren.flaechen(),
                gebuehren.ueberspannungen(),
                gebuehren.verwaltung(),
                gebuehren.gesamt(),
                gebuehren.zahlung(),
                flaechen.abschnitte(),
                ueberspannungen.adressen());
    }
}
